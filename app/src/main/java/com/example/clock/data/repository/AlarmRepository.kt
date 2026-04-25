package com.example.clock.data.repository

import com.example.clock.data.db.AlarmDao
import com.example.clock.data.model.Alarm
import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val dao: AlarmDao) {
    fun getAllAlarms(): Flow<List<Alarm>> = dao.getAllAlarms()
    suspend fun getAlarmById(id: Int): Alarm? = dao.getAlarmById(id)
    suspend fun insertAlarm(alarm: Alarm): Long = dao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: Alarm) = dao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: Alarm) = dao.deleteAlarm(alarm)
    suspend fun setAlarmEnabled(id: Int, enabled: Boolean) = dao.setAlarmEnabled(id, enabled)
    suspend fun getEnabledAlarms(): List<Alarm> = dao.getEnabledAlarms()
}
