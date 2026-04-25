package com.example.clock.data.repository

import com.example.clock.data.db.TimerPresetDao
import com.example.clock.data.model.TimerPreset
import kotlinx.coroutines.flow.Flow

class TimerRepository(private val dao: TimerPresetDao) {
    fun getAllPresets(): Flow<List<TimerPreset>> = dao.getAllPresets()
    suspend fun insertPreset(preset: TimerPreset) = dao.insertPreset(preset)
    suspend fun deletePreset(preset: TimerPreset) = dao.deletePreset(preset)
}
