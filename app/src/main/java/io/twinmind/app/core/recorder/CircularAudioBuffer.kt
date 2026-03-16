package io.twinmind.app.core.recorder

class CircularAudioBuffer(
    val capacity: Int
) {

    private val buffer = ByteArray(capacity)

    private var writeIndex = 0

    fun write(data: ByteArray, length: Int) {

        for (i in 0 until length) {
            buffer[writeIndex] = data[i]
            writeIndex = (writeIndex + 1) % capacity
        }

    }

    fun getOverlap(): ByteArray {
        return buffer.copyOf()
    }
}