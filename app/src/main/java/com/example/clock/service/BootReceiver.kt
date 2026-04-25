package com.example.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.clock.data.db.ClockDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = ClockDatabase.getInstance(context)
                val enabledAlarms = db.alarmDao().getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }
            }
        }
    }
}
