package io.twinmind.app.core.recorder

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class WavWriter(
    val file: File
) {

    private val output = FileOutputStream(file)

    init {
        writeHeader()
    }

    fun write(data: ByteArray, length: Int) {
        output.write(data, 0, length)
    }

    fun close() {
        output.flush()
        output.close()
        updateWavHeader()
    }

    fun updateWavHeader() {
        val totalAudioLength = file.length() - 44
        val totalDataLength = totalAudioLength + 36
        val randomAccessFile = RandomAccessFile(file, "rw")
        randomAccessFile.seek(4)
        randomAccessFile.write(intToLittleEndian(totalDataLength.toInt()))
        randomAccessFile.seek(40)
        randomAccessFile.write(intToLittleEndian(totalAudioLength.toInt()))
        randomAccessFile.close()
    }

    fun intToLittleEndian(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ( (value shr 8) and 0xff).toByte(),
            ( (value shr 16) and 0xff).toByte(),
            ( (value shr 24) and 0xff).toByte()
        )
    }

    fun writeHeader(
        sampleRate: Int = 16000,
        channels: Int = 1,
        bitsPerSample: Int = 16
    ) {

        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteArray(44)

        // RIFF
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // ChunkSize (placeholder)
        writeInt(header, 4, 0)

        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeInt(header, 16, 16) // PCM
        writeShort(header, 20, 1) // AudioFormat
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, blockAlign.toShort())
        writeShort(header, 34, bitsPerSample.toShort())

        // data
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        writeInt(header, 40, 0)

        output.write(header, 0, 44)
    }

    private fun writeInt(buffer: ByteArray, offset: Int, value: Int) {
        buffer[offset] = (value and 0xff).toByte()
        buffer[offset + 1] = ((value shr 8) and 0xff).toByte()
        buffer[offset + 2] = ((value shr 16) and 0xff).toByte()
        buffer[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(buffer: ByteArray, offset: Int, value: Short) {
        buffer[offset] = (value.toInt() and 0xff).toByte()
        buffer[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }

}