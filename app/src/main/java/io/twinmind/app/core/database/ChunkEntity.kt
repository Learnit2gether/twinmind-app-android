package io.twinmind.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chunks")
data class ChunkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val meetingId: String,
    val chunkIndex: String,
    val filePath: String,
    val transcript: String? = null,
    val status: String = "PENDING" // PENDING, PROCESSING, COMPLETED
)