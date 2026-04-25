package com.example.clock.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.clock.data.db.ClockDatabase
import com.example.clock.data.model.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        when (intent.action) {
            "com.example.clock.ALARM_TRIGGER" -> {
                // Start alarm service
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmService.EXTRA_ALARM_LABEL, intent.getStringExtra("alarm_label") ?: "Báo thức")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // Re-schedule if it's a repeating alarm
                CoroutineScope(Dispatchers.IO).launch {
                    val db = ClockDatabase.getInstance(context)
                    val alarm = db.alarmDao().getAlarmById(alarmId)
                    alarm?.let {
                        if (it.repeatDays.isNotEmpty()) {
                            AlarmScheduler.scheduleAlarm(context, it)
                        } else {
                            db.alarmDao().setAlarmEnabled(alarmId, false)
                        }
                    }
                }
            }
            "com.example.clock.ALARM_SNOOZE" -> {
                context.stopService(Intent(context, AlarmService::class.java))
                // Schedule snooze in 5 minutes
                CoroutineScope(Dispatchers.IO).launch {
                    val db = ClockDatabase.getInstance(context)
                    val alarm = db.alarmDao().getAlarmById(alarmId)
                    alarm?.let {
                        val snoozeCalendar = Calendar.getInstance().apply {
                            add(Calendar.MINUTE, it.snoozeMinutes)
                        }
                        AlarmScheduler.scheduleOneShot(context, it, snoozeCalendar)
                    }
                }
            }
            "com.example.clock.ALARM_DISMISS" -> {
                context.stopService(Intent(context, AlarmService::class.java))
            }
        }
    }
}

object AlarmScheduler {
    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.clock.ALARM_TRIGGER"
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_label", alarm.label.ifEmpty { "Báo thức" })
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun scheduleOneShot(context: Context, alarm: Alarm, calendar: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.clock.ALARM_TRIGGER"
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_label", alarm.label.ifEmpty { "Báo thức" })
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.id + 9000, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}
