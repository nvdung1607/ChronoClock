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
        // If skipping a focus phase, still count it as completed
        if (_phase.value == PomodoroPhase.FOCUS) {
            _completedSessions.value++
            _todayPomodoros.value++
        }
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
                if (_isRunning.value) {
                    _remainingSeconds.value -= 1
                }
            }
            if (_remainingSeconds.value <= 0 && _isRunning.value) {
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
                // Long break only after completing enough sessions (never at 0)
                if (completed > 0 && completed % s.sessionsBeforeLongBreak == 0) {
                    PomodoroPhase.LONG_BREAK
                } else {
                    PomodoroPhase.SHORT_BREAK
                }
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

    // Reactive progress for Compose recomposition
    val progressFlow: StateFlow<Float> = combine(_remainingSeconds, _totalSeconds) { remaining, total ->
        if (total == 0L) 0f else (remaining.toFloat() / total).coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1f)
}
