package com.example.clock.ui.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class LapEntry(
    val lapNumber: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long
)

class StopwatchViewModel : ViewModel() {

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _laps = MutableStateFlow<List<LapEntry>>(emptyList())
    val laps: StateFlow<List<LapEntry>> = _laps

    private var job: Job? = null
    private var lastLapMs = 0L

    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        job = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - _elapsedMs.value
            while (isActive && _isRunning.value) {
                _elapsedMs.value = System.currentTimeMillis() - startTime
                delay(10)
            }
        }
    }

    fun pause() {
        _isRunning.value = false
        job?.cancel()
    }

    fun reset() {
        job?.cancel()
        _isRunning.value = false
        _elapsedMs.value = 0L
        _laps.value = emptyList()
        lastLapMs = 0L
    }

    fun lap() {
        val total = _elapsedMs.value
        val lapTime = total - lastLapMs
        lastLapMs = total
        val lapNumber = _laps.value.size + 1
        _laps.value = listOf(LapEntry(lapNumber, lapTime, total)) + _laps.value
    }

    val fastestLapIndex: Int
        get() {
            val laps = _laps.value
            if (laps.size < 2) return -1
            return laps.indices.minByOrNull { laps[it].lapTimeMs } ?: -1
        }

    val slowestLapIndex: Int
        get() {
            val laps = _laps.value
            if (laps.size < 2) return -1
            return laps.indices.maxByOrNull { laps[it].lapTimeMs } ?: -1
        }
}

fun formatStopwatchTime(ms: Long): String {
    val h = ms / 3600000
    val m = (ms % 3600000) / 60000
    val s = (ms % 60000) / 1000
    val cs = (ms % 1000) / 10
    return if (h > 0) "%02d:%02d:%02d.%02d".format(h, m, s, cs)
    else "%02d:%02d.%02d".format(m, s, cs)
}

fun formatLapTime(ms: Long): String {
    val m = ms / 60000
    val s = (ms % 60000) / 1000
    val cs = (ms % 1000) / 10
    return "%02d:%02d.%02d".format(m, s, cs)
}
