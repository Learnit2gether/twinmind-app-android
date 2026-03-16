package io.twinmind.app.core.recorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import javax.inject.Inject

private const val TAG = "MeetingRecorder"

@SuppressLint("MissingPermission")
class MeetingRecorder @Inject constructor() {

    private val minBufferSize =
        AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    private lateinit var audioRecord: AudioRecord


    fun getAudioRecord(): AudioRecord {
        if (!::audioRecord.isInitialized) {
            audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        /**
                         * 16000 samples/sec * 2 bytes/sample
                         * Size = 16000 * 2 = 32000 bytes/sec
                         * 32000 * 30 sec = 960000 bytes ~ 0.96MB < 1MB
                         */
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build()
                )
                .setBufferSizeInBytes(2 * minBufferSize)
                .build()
        }
        return audioRecord
    }

    fun getMinBufferSize() = 2 * minBufferSize

    fun start() {
        Log.d(TAG, "Meeting recorder start: ")
        getAudioRecord().startRecording()
    }

    fun read(buffer: ByteArray): Int {
        Log.d(TAG, "Meeting recorder read: ")
        return getAudioRecord().read(buffer, 0, buffer.size)
    }

    fun release() {
        getAudioRecord().release()
    }

    fun stop() {
        getAudioRecord().stop()
    }



    companion object {
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val SAMPLE_RATE = 16000

        //2 bytes / sample = 16 BIT encoding = 2 bytes
        private const val BYTE_PER_SEC = 2 * SAMPLE_RATE

        // chunk of 30 sec
        const val CHUNK_DURATION = 30000
        const val CHUNK_SIZE = 30 * BYTE_PER_SEC
        const val OVERLAP_CHUNK_SIZE = 2 * BYTE_PER_SEC
    }

}