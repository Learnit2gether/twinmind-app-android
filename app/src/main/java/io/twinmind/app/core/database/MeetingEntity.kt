package io.twinmind.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val title: String = "Unknown Meeting",
    val startTime: Long,
    val endTime: Long = -1L,
    val meetingDuration: Long = -1L,
    val status: String = "RECORDING", // RECORDING, COMPLETED
    val summary: String? = null,
)