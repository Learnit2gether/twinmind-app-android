package io.twinmind.app.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.twinmind.app.core.database.ChunkEntity
import io.twinmind.app.core.database.MeetingEntity
import io.twinmind.app.data.MeetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MeetingRepository,
) : ViewModel() {

    // Tab logic
    val tabs = listOf("Notes", "Transcript")


    // Get meeting data by ID
    fun getMeeting(id: String): Flow<MeetingEntity?> {
        return repository.getMeetingSummary(id)
    }

    // Get chunks for transcript
    fun getChunks(id: String): Flow<List<ChunkEntity>> {
        return repository.getChunksWithTranscript(id)
    }

    // Triggered when user enters the screen
    fun ensureSummaryGenerated(meetingId: String) {
        viewModelScope.launch {
            repository.getStreamingSummary(meetingId).collect { /* Repository handles DB update */ }
        }
    }
}