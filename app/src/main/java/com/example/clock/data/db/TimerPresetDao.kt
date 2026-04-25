package com.example.clock.data.db

import androidx.room.*
import com.example.clock.data.model.TimerPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPresetDao {
    @Query("SELECT * FROM timer_presets ORDER BY label")
    fun getAllPresets(): Flow<List<TimerPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: TimerPreset)

    @Delete
    suspend fun deletePreset(preset: TimerPreset)
}
