package com.example.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.clock.MainActivity
import com.example.clock.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.example.clock.TIMER_START"
        const val ACTION_PAUSE = "com.example.clock.TIMER_PAUSE"
        const val ACTION_STOP = "com.example.clock.TIMER_STOP"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_TIMER_LABEL = "timer_label"
    }

    private val binder = TimerBinder()
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private var totalSeconds = 0L
    private var timerLabel = "Bộ hẹn giờ"

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0L)
                timerLabel = intent.getStringExtra(EXTRA_TIMER_LABEL) ?: "Bộ hẹn giờ"
                startTimer(duration)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    fun startTimer(seconds: Long) {
        if (seconds <= 0) return
        totalSeconds = seconds
        _remainingSeconds.value = seconds
        _isFinished.value = false
        _isRunning.value = true
        startForeground(NOTIFICATION_ID, buildNotification(seconds))
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000)
                _remainingSeconds.value -= 1
                updateNotification(_remainingSeconds.value)
            }
            if (_remainingSeconds.value <= 0) {
                _isFinished.value = true
                _isRunning.value = false
                showFinishedNotification()
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (_remainingSeconds.value > 0 && !_isRunning.value) {
            startTimer(_remainingSeconds.value)
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isRunning.value = false
        _remainingSeconds.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(seconds: Long): Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        val timeStr = if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(timerLabel)
            .setContentText("Còn lại: $timeStr")
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(seconds: Long) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(seconds))
    }

    private fun showFinishedNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        val mainIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle("⏰ $timerLabel xong rồi!")
            .setContentText("Bộ hẹn giờ đã kết thúc")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID + 1, notification)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bộ Hẹn Giờ",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo đếm ngược"
                setSound(null, null)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder
}
