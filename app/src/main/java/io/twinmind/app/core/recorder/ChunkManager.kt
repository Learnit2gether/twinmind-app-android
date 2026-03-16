package io.twinmind.app.core.recorder

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.twinmind.app.data.MeetingRepository
import java.io.File
import javax.inject.Inject

class ChunkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meetingRepository: MeetingRepository
) {

    private var currentChunk: WavWriter? = null

    private var audioFile: File? = null

    private val overlapBuffer = CircularAudioBuffer(MeetingRecorder.OVERLAP_CHUNK_SIZE)

    private var chunkStartTime = System.currentTimeMillis()

    private var meetingStartTime: Long = 0

    private lateinit var meetingId: String

    private var chunkIndex = 0

    suspend fun processAudio(data: ByteArray, length: Int) {

        overlapBuffer.write(data, length)

        currentChunk?.write(data, length)

        if (System.currentTimeMillis() - chunkStartTime >= MeetingRecorder.CHUNK_DURATION) {

            finalizeChunk()

            startNextChunk()

        }
    }

    fun startNextChunk() {

        val file = createChunkFile()

        audioFile = file

        currentChunk = WavWriter(file)

        val overlap = overlapBuffer.getOverlap()

        currentChunk?.write(overlap, overlap.size)

        chunkStartTime = System.currentTimeMillis()

    }

    suspend fun finalizeChunk() {
        audioFile?.let { file ->
            meetingRepository.handleNewChunk(meetingId, file.absolutePath, file.name)
        }

        currentChunk?.close()

        currentChunk = null
    }

    suspend fun finishMeeting() {
        val endTime = System.currentTimeMillis()
        meetingRepository.updateMeetingCompleted(meetingId, endTime, endTime - meetingStartTime )
    }

    suspend fun startMeeting(meetingId: String) {
        this.meetingId = meetingId
        this.chunkIndex = 0
        meetingStartTime = System.currentTimeMillis()
        meetingRepository.insertMeeting(meetingId, meetingStartTime)
        startNextChunk()
    }

    private fun createChunkFile(): File {
        val meetingsDir = File(context.filesDir, "meetings")
        if (!meetingsDir.exists()) {
            meetingsDir.mkdirs()
        }

        val meetingDir = File(meetingsDir, "$meetingId")
        if (!meetingDir.exists()){
            meetingDir.mkdirs()
        }
        chunkIndex++;
        val fileName = "chunk_${chunkIndex.toString().padStart(3,'0')}.wav"

        val newFile =  File(meetingDir, fileName)

        return newFile
    }
}

