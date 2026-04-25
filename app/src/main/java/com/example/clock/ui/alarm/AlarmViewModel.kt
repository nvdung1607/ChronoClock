package com.example.clock.ui.alarm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clock.data.db.ClockDatabase
import com.example.clock.data.model.Alarm
import com.example.clock.data.repository.AlarmRepository
import com.example.clock.service.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class AlarmViewModel(private val context: Context) : ViewModel() {
    private val repository = AlarmRepository(
        ClockDatabase.getInstance(context).alarmDao()
    )

    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val id = repository.insertAlarm(alarm)
            val newAlarm = alarm.copy(id = id.toInt())
            if (newAlarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(context, newAlarm)
            }
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
            AlarmScheduler.cancelAlarm(context, alarm.id)
            if (alarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(context, alarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(context, alarm.id)
            repository.deleteAlarm(alarm)
        }
    }

    fun toggleAlarm(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            repository.setAlarmEnabled(alarm.id, enabled)
            if (enabled) {
                AlarmScheduler.scheduleAlarm(context, alarm.copy(isEnabled = true))
            } else {
                AlarmScheduler.cancelAlarm(context, alarm.id)
            }
        }
    }

    fun getTimeUntilAlarm(alarm: Alarm): String {
        val now = Calendar.getInstance()
        val alarmCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val diffMs = alarmCal.timeInMillis - now.timeInMillis
        val diffHours = diffMs / 3600000
        val diffMinutes = (diffMs % 3600000) / 60000
        return when {
            diffHours > 0 -> "Còn ${diffHours}h ${diffMinutes}m nữa"
            diffMinutes > 0 -> "Còn ${diffMinutes} phút nữa"
            else -> "Sắp đến"
        }
    }
}
