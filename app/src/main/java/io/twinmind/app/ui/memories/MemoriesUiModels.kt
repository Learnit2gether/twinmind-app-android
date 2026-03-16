package io.twinmind.app.ui.memories

/**
 * UI-specific model to avoid formatting logic inside Composable functions.
 */
data class MeetingItemUiModel(
    val id: String,
    val title: String,
    val displayTime: String,     // e.g., "07:48 pm"
    val displayDuration: String, // e.g., "5 min"
    val dateHeader: String       // e.g., "Sun, Mar 15" - used for grouping
)

sealed class MemoriesUiState {
    object Loading : MemoriesUiState()
    data class Success(val groupedMeetings: Map<String, List<MeetingItemUiModel>>) : MemoriesUiState()
    object Empty : MemoriesUiState()
}