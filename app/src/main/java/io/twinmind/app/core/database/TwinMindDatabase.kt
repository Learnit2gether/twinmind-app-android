package io.twinmind.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MeetingEntity::class, ChunkEntity::class], version = 1)
abstract class TwinMindDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
}
