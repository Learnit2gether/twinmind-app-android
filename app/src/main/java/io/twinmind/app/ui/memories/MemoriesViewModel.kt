package io.twinmind.app.ui.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.twinmind.app.core.database.MeetingDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val dao: MeetingDao
) : ViewModel() {

    // Helper Formatters
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val headerFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())

    val uiState: StateFlow<MemoriesUiState> = dao.getAllMeetingsFlow()
        .map { entities ->
            if (entities.isEmpty()) {
                MemoriesUiState.Empty
            } else {
                // Transform and Group logic
                val grouped = entities.map { entity ->
                    MeetingItemUiModel(
                        id = entity.id,
                        title = entity.title.ifBlank { "Untitled Meeting" },
                        displayTime = timeFormatter.format(Date(entity.startTime)),
                        // We calculate duration based on end of recording or chunks
                        displayDuration = "0 min",
                        dateHeader = headerFormatter.format(Date(entity.startTime))
                    )
                }.groupBy { it.dateHeader }

                MemoriesUiState.Success(grouped)
            }
        }
        .flowOn(Dispatchers.Default) // Move processing off the Main thread
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MemoriesUiState.Loading
        )
}