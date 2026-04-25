package com.example.clock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_presets")
data class TimerPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val durationSeconds: Long,
    val emoji: String = "⏱️"
)
