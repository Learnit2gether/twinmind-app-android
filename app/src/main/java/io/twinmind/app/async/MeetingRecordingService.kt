package io.twinmind.app.async

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.twinmind.app.TAG
import io.twinmind.app.core.notifications.AppNotifications
import io.twinmind.app.core.notifications.AppNotifications.Companion.NOTIFICATION_ID
import io.twinmind.app.core.recorder.ChunkManager
import io.twinmind.app.core.recorder.MeetingRecorder
import io.twinmind.app.core.generateMeetingId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MeetingRecordingService : Service() {

    @Inject
    lateinit var chunkManager: ChunkManager
    @Inject
    lateinit var audioRecorder: Lazy<MeetingRecorder>
    @Inject
    lateinit var appNotifications: AppNotifications
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recordingJob: Job? = null
    private var startTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MeetingRecordingService onCreate")
        startForeground(
            NOTIFICATION_ID,
            appNotifications.buildNotification(applicationContext, "Don'tKnow")
        )
        registerPhoneStateListener()
        requestAudioFocus()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand: Intent = ${intent?.action} :: Flags$flags")
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        Log.d(TAG, "MeetingRecordingService onStartCommand")
        return START_STICKY
    }

    private fun startRecording() {
        if (recordingJob != null) return
        recordingJob = serviceScope.launch {
            startTime = System.currentTimeMillis()
            val meetingId = generateMeetingId()
            chunkManager.startMeeting(meetingId)
            audioRecorder.get().start()
            val buffer = ByteArray(audioRecorder.get().getMinBufferSize())
            while (isActive) {
                val read = audioRecorder.get().read(buffer)
                if (read > 0) {
                    chunkManager.processAudio(buffer.copyOf(read), read)
                }
            }
        }
    }

    private fun pauseRecording() {
        recordingJob?.cancel()
        appNotifications.updateNotification(NOTIFICATION_ID, "Paused")
    }

    private fun resumeRecording() {
        startRecording()
        appNotifications.updateNotification(NOTIFICATION_ID, "Recording...")
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        audioRecorder.get().stop()
        serviceScope.launch {
            chunkManager.finalizeChunk()
            chunkManager.finishMeeting()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


    override fun onDestroy() {
        recordingJob?.cancel()
        audioRecorder.get().release()
        super.onDestroy()
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val focusRequest = AudioFocusRequest.Builder(
            AudioManager.AUDIOFOCUS_GAIN
        ).setOnAudioFocusChangeListener {
                when (it) {
                    AudioManager.AUDIOFOCUS_LOSS -> pauseRecording()
                    AudioManager.AUDIOFOCUS_GAIN -> resumeRecording()
                }
            }
            .build()
        audioManager.requestAudioFocus(focusRequest)
    }

    private fun registerPhoneStateListener() {
        val telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val telephonyListener =
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        when (state) {
                            TelephonyManager.CALL_STATE_RINGING,
                            TelephonyManager.CALL_STATE_OFFHOOK -> pauseRecording()
                            TelephonyManager.CALL_STATE_IDLE -> resumeRecording()
                        }
                    }
                }
            telephonyManager.registerTelephonyCallback(mainExecutor, telephonyListener)
        } else {
            telephonyManager.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING,
                        TelephonyManager.CALL_STATE_OFFHOOK -> pauseRecording()
                        TelephonyManager.CALL_STATE_IDLE -> resumeRecording()
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_START = "io.twinmind.app.action.start"
        const val ACTION_PAUSE = "io.twinmind.app.action.pause"
        const val ACTION_RESUME = "io.twinmind.app.action.resume"
        const val ACTION_STOP = "io.twinmind.app.action.stop"
    }
}