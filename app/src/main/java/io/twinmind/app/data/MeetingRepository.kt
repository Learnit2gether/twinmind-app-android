package io.twinmind.app.data

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.twinmind.app.TAG
import io.twinmind.app.async.TranscriptionWorker
import io.twinmind.app.core.database.ChunkEntity
import io.twinmind.app.core.database.MeetingDao
import io.twinmind.app.core.database.MeetingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeetingRepository @Inject constructor(
    private val dao: MeetingDao,
    private val workManager: WorkManager,
    private val gemini: GeminiRepository
) {

    fun getChunksWithTranscript(meetingId: String): Flow<List<ChunkEntity>> = dao.getChunksFlow(meetingId)

    fun getMeetingSummary(meetingId: String) = dao.getMeetingByIdFlow(meetingId)

    suspend fun updateMeetingCompleted(meetingId: String, endTimeUnit: Long, durationUnit: Long) {
        dao.updatedCompletedMeeting(meetingId, endTimeUnit, durationUnit)
    }

    suspend fun insertMeeting(meetingId: String, startTime: Long) {
        dao.insertMeeting(MeetingEntity(id = meetingId, startTime = startTime))
    }

    suspend fun handleNewChunk(meetingId: String, path: String, index: String) {
        val id = dao.insertChunk(
            ChunkEntity(
                meetingId = meetingId,
                chunkIndex = index,
                filePath = path
            )
        )

        val work = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(workDataOf("CHUNK_ID" to id, "FILE_PATH" to path))
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            // Make it Expedited for immediate execution
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            // Add a Backoff policy for the "Retry if failure" requirement
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS
            )
            .build()
        workManager.enqueue(work)
    }

    fun getStreamingSummary(meetingId: String): Flow<String> = flow {
        val existingMeeting = dao.getMeetingById(meetingId)
        if (!existingMeeting?.summary.isNullOrBlank()) {
            emit(existingMeeting.summary)
            return@flow
        }

        val fullTranscript = StringBuilder()
        val batchSize = 50 // Fetch 50 chunks at a time
        var currentOffset = 0
        var hasMore = true

        while (hasMore) {
            val batch = dao.getChunksBatch(meetingId, batchSize, currentOffset)

            if (batch.isEmpty()) {
                hasMore = false
            } else {
                batch.forEach { chunk ->
                    if (!chunk.transcript.isNullOrBlank()) {
                        fullTranscript.append(chunk.transcript).append(" ")
                    }
                }
                currentOffset += batchSize
            }
        }

        val transcriptString = fullTranscript.toString().trim()

        // 3. AI Stream Processing
        var accumulatedSummary = ""
        gemini.summarizeTranscriptStream(transcriptString).catch { e ->
            emit("error processing stream ${e.message}")
            Log.e(TAG, "error processing stream ${e.message}" )
        }.collect { partial ->
            accumulatedSummary += partial
            emit(accumulatedSummary)

            // 1. Extract and update Title if we haven't yet
            if (accumulatedSummary.contains("# ")) {
                val extractedTitle = extractTitleFromMarkdown(accumulatedSummary)
                dao.updateSummary(meetingId, accumulatedSummary, extractedTitle)
            }
        }
    }

    private fun extractTitleFromMarkdown(text: String): String {
        return Regex("^#\\s*(.*)", RegexOption.MULTILINE)
            .find(text)?.groupValues?.get(1)?.trim() ?: "New Meeting"
    }
}