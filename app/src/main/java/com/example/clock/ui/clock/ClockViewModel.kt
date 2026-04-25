package com.example.clock.ui.clock

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clock.data.db.ClockDatabase
import com.example.clock.data.model.WorldClock
import com.example.clock.data.repository.WorldClockRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ClockViewModel(context: Context) : ViewModel() {
    private val repository = WorldClockRepository(
        ClockDatabase.getInstance(context).worldClockDao()
    )

    val worldClocks: StateFlow<List<WorldClock>> = repository.getAllWorldClocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWorldClock(worldClock: WorldClock) {
        viewModelScope.launch { repository.insertWorldClock(worldClock) }
    }

    fun deleteWorldClock(worldClock: WorldClock) {
        viewModelScope.launch { repository.deleteWorldClock(worldClock) }
    }
}

fun getTimeForTimezone(timeZoneId: String): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(timeZoneId)
    }
    return sdf.format(Date())
}

fun getDateForTimezone(timeZoneId: String): String {
    val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(timeZoneId)
    }
    return sdf.format(Date())
}

fun getGmtOffset(timeZoneId: String): String {
    val tz = TimeZone.getTimeZone(timeZoneId)
    val offsetMs = tz.getOffset(System.currentTimeMillis())
    val offsetHours = offsetMs / 3600000
    val offsetMins = (offsetMs % 3600000) / 60000
    return if (offsetMins == 0) {
        "GMT${if (offsetHours >= 0) "+" else ""}$offsetHours"
    } else {
        "GMT${if (offsetHours >= 0) "+" else ""}$offsetHours:${"%02d".format(Math.abs(offsetMins))}"
    }
}

fun isDaytime(timeZoneId: String): Boolean {
    val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    return hour in 6..18
}
