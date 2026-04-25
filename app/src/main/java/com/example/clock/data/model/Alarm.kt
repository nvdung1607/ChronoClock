package com.example.clock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: String = "",   // e.g. "1,3,5" for Mon, Wed, Fri. Empty = once
    val soundUri: String = "",
    val isVibrate: Boolean = true,
    val snoozeMinutes: Int = 5
)
