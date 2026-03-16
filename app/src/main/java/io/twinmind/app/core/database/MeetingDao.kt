package io.twinmind.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity)

    @Query("UPDATE meetings SET status = 'COMPLETED', endTime = :endTimeUnit, meetingDuration = :durationUnit WHERE id = :meetingId")
    suspend fun updatedCompletedMeeting(meetingId: String, endTimeUnit: Long, durationUnit: Long)

    @Query("SELECT * FROM meetings  ORDER BY startTime DESC")
    fun getAllMeetingsFlow(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :meetingId LIMIT 1")
    suspend fun getMeetingById(meetingId: String): MeetingEntity?


    @Query("SELECT * FROM meetings WHERE id = :meetingId LIMIT 1")
    fun getMeetingByIdFlow(meetingId: String): Flow<MeetingEntity?>

    @Query("SELECT * FROM chunks WHERE meetingId = :meetingId ORDER BY chunkIndex ASC")
    fun getChunksFlow(meetingId: String): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks WHERE meetingId = :meetingId ORDER BY chunkIndex ASC")
    suspend fun getChunksSync(meetingId: String): List<ChunkEntity>

    @Insert
    suspend fun insertChunk(chunk: ChunkEntity): Long

    @Query("UPDATE chunks SET transcript = :text, status = 'COMPLETED' WHERE id = :id")
    suspend fun updateChunkTranscript(id: Long, text: String)

    @Query("UPDATE meetings SET summary = :summary, title = :title WHERE id = :meetingId")
    suspend fun updateSummary(meetingId: String, summary: String, title: String)

    @Query("UPDATE chunks SET status = :text WHERE id = :id")
    suspend fun updateChunkStatus(id: Long, text: String)

    @Query("SELECT * FROM chunks WHERE meetingId = :meetingId ORDER BY chunkIndex ASC LIMIT :limit OFFSET :offset")
    suspend fun getChunksBatch(meetingId: String, limit: Int, offset: Int): List<ChunkEntity>
}