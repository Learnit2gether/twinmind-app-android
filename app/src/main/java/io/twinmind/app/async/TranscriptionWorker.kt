package io.twinmind.app.async

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.twinmind.app.TAG
import io.twinmind.app.core.notifications.AppNotifications
import io.twinmind.app.core.database.MeetingDao
import io.twinmind.app.data.GeminiRepository
import java.io.File

@HiltWorker
class TranscriptionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val geminiRepository: GeminiRepository,
    private val dao: MeetingDao,
    private val appNotifications: AppNotifications
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Started")
        val chunkId = inputData.getLong("CHUNK_ID", -1)
        Log.d(TAG, "doWork: chunkId $chunkId")
        val filePath = inputData.getString("FILE_PATH") ?: return Result.failure()
        Log.d(TAG, "doWork: path $filePath")

        return try {
            // 1. Update status to Processing
            dao.updateChunkStatus(chunkId, "PROCESSING")

            // 2. Call Gemini (On-device or API)
            val file = File(filePath)
            val result = geminiRepository.transcribeAudioFile(file)
            Log.d(TAG, "doWork: $result")
            if (result != null) {
                // 3. Save to Room (Single Source of Truth)
                dao.updateChunkTranscript(chunkId, result)
                Result.success()
            } else {
                Result.retry() // Triggers automatic backoff/retry
            }
        } catch (e: Exception) {
            Log.d(TAG, "doWork: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = appNotifications.createNotificationForTranscription()
        return ForegroundInfo(AppNotifications.NOTIFICATION_ID, notification)
    }
}