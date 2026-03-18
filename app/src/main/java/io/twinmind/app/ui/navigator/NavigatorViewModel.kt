package io.twinmind.app.ui.navigator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.twinmind.app.core.generateMeetingId
import io.twinmind.app.domain.MeetingRecordingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UiModel(val isRecording: Boolean)

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    val meetingRecordingUseCase: MeetingRecordingUseCase,
): ViewModel() {

    private val _uiStateFlow = MutableStateFlow(UiModel(false))
    val uiStateFlow = _uiStateFlow.asStateFlow()

    // todo - check runtime permission foreground, microphone, read phone state
    fun startRecording() {
        meetingRecordingUseCase.startMeeting()
        _uiStateFlow.update { UiModel(true) }
    }

    fun stopRecording() {
        _uiStateFlow.update {UiModel(false)  }
        meetingRecordingUseCase.stopMeeting()
    }

}