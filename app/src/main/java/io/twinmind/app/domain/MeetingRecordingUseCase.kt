package io.twinmind.app.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.twinmind.app.async.MeetingRecordingService
import javax.inject.Inject

class MeetingRecordingUseCase @Inject constructor(
   @ApplicationContext private val applicationContext: Context
){
    fun startMeeting(meetingId: String) {
        val intent = Intent(applicationContext, MeetingRecordingService::class.java).apply {
            action = MeetingRecordingService.ACTION_START
        }
        ContextCompat.startForegroundService(applicationContext,intent)
    }

    fun stopMeeting() {
        val intent = Intent(applicationContext, MeetingRecordingService::class.java).apply {
            action = MeetingRecordingService.ACTION_STOP
        }
        applicationContext.startService(intent)
    }
}