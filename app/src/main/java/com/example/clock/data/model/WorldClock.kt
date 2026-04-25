package com.example.clock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_clocks")
data class WorldClock(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,   // e.g. "America/New_York"
    val sortOrder: Int = 0
)
