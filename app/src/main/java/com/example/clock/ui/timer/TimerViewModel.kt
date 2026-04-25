package com.example.clock.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

enum class TimerInputMode { DURATION, TIME_RANGE }
enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }

class TimerViewModel : ViewModel() {

    // Input mode toggle
    private val _inputMode = MutableStateFlow(TimerInputMode.DURATION)
    val inputMode: StateFlow<TimerInputMode> = _inputMode

    // Duration input (digits string like "015000" = 1m 50s)
    private val _digitInput = MutableStateFlow("")
    val digitInput: StateFlow<String> = _digitInput

    // Time range mode
    private val _startHour = MutableStateFlow(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    private val _startMinute = MutableStateFlow(Calendar.getInstance().get(Calendar.MINUTE))
    private val _endHour = MutableStateFlow((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24)
    private val _endMinute = MutableStateFlow(Calendar.getInstance().get(Calendar.MINUTE))
    val startHour: StateFlow<Int> = _startHour
    val startMinute: StateFlow<Int> = _startMinute
    val endHour: StateFlow<Int> = _endHour
    val endMinute: StateFlow<Int> = _endMinute

    // Timer state
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds

    private val _totalSeconds = MutableStateFlow(0L)
    val totalSeconds: StateFlow<Long> = _totalSeconds

    private var job: Job? = null

    fun setInputMode(mode: TimerInputMode) { _inputMode.value = mode }

    fun onDigit(d: Int) {
        if (_digitInput.value.length < 6) {
            _digitInput.value = _digitInput.value + d.toString()
        }
    }

    fun onDoubleZero() {
        repeat(2) { onDigit(0) }
    }

    fun onDelete() {
        if (_digitInput.value.isNotEmpty()) {
            _digitInput.value = _digitInput.value.dropLast(1)
        }
    }

    fun setStartTime(h: Int, m: Int) { _startHour.value = h; _startMinute.value = m }
    fun setEndTime(h: Int, m: Int) { _endHour.value = h; _endMinute.value = m }

    private fun getDurationFromInput(): Long {
        val s = _digitInput.value.padStart(6, '0')
        val h = s.substring(0, 2).toLongOrNull() ?: 0
        val m = s.substring(2, 4).toLongOrNull() ?: 0
        val sec = s.substring(4, 6).toLongOrNull() ?: 0
        return h * 3600 + m * 60 + sec
    }

    private fun getDurationFromRange(): Long {
        val now = Calendar.getInstance()
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _endHour.value)
            set(Calendar.MINUTE, _endMinute.value)
            set(Calendar.SECOND, 0)
        }
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _startHour.value)
            set(Calendar.MINUTE, _startMinute.value)
            set(Calendar.SECOND, 0)
        }
        if (end.before(start)) end.add(Calendar.DAY_OF_MONTH, 1)
        val diffMs = end.timeInMillis - maxOf(now.timeInMillis, start.timeInMillis)
        return if (diffMs > 0) diffMs / 1000 else 0
    }

    fun startTimer() {
        val duration = when (_inputMode.value) {
            TimerInputMode.DURATION -> getDurationFromInput()
            TimerInputMode.TIME_RANGE -> getDurationFromRange()
        }
        if (duration <= 0) return
        _totalSeconds.value = duration
        _remainingSeconds.value = duration
        _timerState.value = TimerState.RUNNING
        runCountdown()
    }

    fun pauseTimer() {
        job?.cancel()
        _timerState.value = TimerState.PAUSED
    }

    fun resumeTimer() {
        _timerState.value = TimerState.RUNNING
        runCountdown()
    }

    fun stopTimer() {
        job?.cancel()
        _timerState.value = TimerState.IDLE
        _remainingSeconds.value = 0
        _totalSeconds.value = 0
        _digitInput.value = ""
    }

    fun resetFromFinished() {
        _timerState.value = TimerState.IDLE
        _remainingSeconds.value = 0
        _totalSeconds.value = 0
    }

    private fun runCountdown() {
        job?.cancel()
        job = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _timerState.value == TimerState.RUNNING) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0 && _timerState.value == TimerState.RUNNING) {
                _timerState.value = TimerState.FINISHED
            }
        }
    }

    val progress: Float
        get() {
            val total = _totalSeconds.value
            if (total == 0L) return 0f
            return (_remainingSeconds.value.toFloat() / total).coerceIn(0f, 1f)
        }
}

fun formatTimerDisplay(seconds: Long): Triple<String, String, String> {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return Triple("%02d".format(h), "%02d".format(m), "%02d".format(s))
}

fun formatDigitInput(raw: String): String {
    val padded = raw.padStart(6, '0')
    val h = padded.substring(0, 2)
    val m = padded.substring(2, 4)
    val s = padded.substring(4, 6)
    return "$h:$m:$s"
}
