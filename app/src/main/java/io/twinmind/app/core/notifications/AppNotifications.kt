package io.twinmind.app.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.twinmind.app.R
import io.twinmind.app.async.MeetingRecordingService
import javax.inject.Inject

class AppNotifications @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    init {
        createNotificationChannel()
    }

    fun buildNotification(context: Context, status: String): Notification {

        val pauseIntent = Intent(context, MeetingRecordingService::class.java).apply {
            action = MeetingRecordingService.Companion.ACTION_PAUSE
        }

        val stopIntent = Intent(context, MeetingRecordingService::class.java).apply {
            action = MeetingRecordingService.Companion.ACTION_STOP
        }

        val pausePendingIntent = PendingIntent.getService(
            context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            context, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Recording")
            .setContentText(status)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.mipmap.ic_launcher, "Pause", pausePendingIntent)
            .addAction(R.mipmap.ic_launcher, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setRequestPromotedOngoing(true) // Required for Live Update status
            .setOnlyAlertOnce(true) // Prevents sound on every small progress update
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        val name = "Recording Channel"
        val descriptionText = "Progress of the work shown here"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun updateNotification(id: Int, status: String) {
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val recordingNotification = buildNotification(applicationContext, status)
        notificationManager.notify(id, recordingNotification)
    }


    fun createNotificationForTranscription(): Notification {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("TwinMind AI")
            .setContentText("Syncing your notes...")
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "999"
    }
}