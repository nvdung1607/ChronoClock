package com.example.clock.ui.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class PomodoroPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

data class PomodoroSettings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4
)

class PomodoroViewModel : ViewModel() {

    private val _settings = MutableStateFlow(PomodoroSettings())
    val settings: StateFlow<PomodoroSettings> = _settings

    private val _phase = MutableStateFlow(PomodoroPhase.FOCUS)
    val phase: StateFlow<PomodoroPhase> = _phase

    private val _remainingSeconds = MutableStateFlow(25 * 60L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds

    private val _totalSeconds = MutableStateFlow(25 * 60L)
    val totalSeconds: StateFlow<Long> = _totalSeconds

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _completedSessions = MutableStateFlow(0)
    val completedSessions: StateFlow<Int> = _completedSessions

    private val _todayPomodoros = MutableStateFlow(0)
    val todayPomodoros: StateFlow<Int> = _todayPomodoros

    private var job: Job? = null

    fun updateSettings(settings: PomodoroSettings) {
        _settings.value = settings
        if (!_isRunning.value) {
            setPhase(PomodoroPhase.FOCUS)
        }
    }

    fun toggle() {
        if (_isRunning.value) pause() else start()
    }

    fun skip() {
        job?.cancel()
        _isRunning.value = false
        advancePhase()
    }

    fun reset() {
        job?.cancel()
        _isRunning.value = false
        _completedSessions.value = 0
        setPhase(PomodoroPhase.FOCUS)
    }

    private fun start() {
        _isRunning.value = true
        job?.cancel()
        job = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0) {
                _isRunning.value = false
                if (_phase.value == PomodoroPhase.FOCUS) {
                    _completedSessions.value++
                    _todayPomodoros.value++
                }
                delay(500)
                advancePhase()
            }
        }
    }

    private fun pause() {
        _isRunning.value = false
        job?.cancel()
    }

    private fun advancePhase() {
        val s = _settings.value
        val completed = _completedSessions.value
        val nextPhase = when (_phase.value) {
            PomodoroPhase.FOCUS -> {
                if (completed % s.sessionsBeforeLongBreak == 0) PomodoroPhase.LONG_BREAK
                else PomodoroPhase.SHORT_BREAK
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> PomodoroPhase.FOCUS
        }
        setPhase(nextPhase)
    }

    private fun setPhase(p: PomodoroPhase) {
        _phase.value = p
        val secs = when (p) {
            PomodoroPhase.FOCUS -> _settings.value.focusMinutes * 60L
            PomodoroPhase.SHORT_BREAK -> _settings.value.shortBreakMinutes * 60L
            PomodoroPhase.LONG_BREAK -> _settings.value.longBreakMinutes * 60L
        }
        _remainingSeconds.value = secs
        _totalSeconds.value = secs
    }

    val progress: Float
        get() {
            val total = _totalSeconds.value
            if (total == 0L) return 0f
            return (_remainingSeconds.value.toFloat() / total).coerceIn(0f, 1f)
        }
}
