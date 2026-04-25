package com.example.clock.ui.timer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clock.ui.components.CircularProgressArc
import com.example.clock.ui.components.NumPad
import com.example.clock.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(vm: TimerViewModel = viewModel()) {
    val timerState by vm.timerState.collectAsState()
    val remainingSeconds by vm.remainingSeconds.collectAsState()
    val totalSeconds by vm.totalSeconds.collectAsState()
    val inputMode by vm.inputMode.collectAsState()
    val digitInput by vm.digitInput.collectAsState()
    val startHour by vm.startHour.collectAsState()
    val startMinute by vm.startMinute.collectAsState()
    val endHour by vm.endHour.collectAsState()
    val endMinute by vm.endMinute.collectAsState()
    val progress by vm.progress.collectAsState()

    val isLow = remainingSeconds < 30 && timerState == TimerState.RUNNING
    val progressColor = if (isLow) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary

    // Pulse animation when low
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isLow) 0.4f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode tabs (only shown when idle)
        if (timerState == TimerState.IDLE) {
            TabRow(
                selectedTabIndex = if (inputMode == TimerInputMode.DURATION) 0 else 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = inputMode == TimerInputMode.DURATION,
                    onClick = { vm.setInputMode(TimerInputMode.DURATION) },
                    text = { Text("⏱ Thời lượng") }
                )
                Tab(
                    selected = inputMode == TimerInputMode.TIME_RANGE,
                    onClick = { vm.setInputMode(TimerInputMode.TIME_RANGE) },
                    text = { Text("🕐 Giờ bắt đầu → kết thúc") }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        when {
            timerState == TimerState.IDLE && inputMode == TimerInputMode.DURATION -> {
                DurationInputMode(
                    digitInput = digitInput,
                    onDigit = vm::onDigit,
                    onDoubleZero = vm::onDoubleZero,
                    onDelete = vm::onDelete,
                    onStart = vm::startTimer
                )
            }

            timerState == TimerState.IDLE && inputMode == TimerInputMode.TIME_RANGE -> {
                TimeRangeInputMode(
                    startHour = startHour, startMinute = startMinute,
                    endHour = endHour, endMinute = endMinute,
                    onSetStart = vm::setStartTime,
                    onSetEnd = vm::setEndTime,
                    onStart = vm::startTimer
                )
            }

            timerState == TimerState.FINISHED -> {
                FinishedState(onReset = vm::resetFromFinished)
            }

            else -> {
                // Countdown running / paused
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressArc(
                        modifier = Modifier.fillMaxSize(),
                        progress = progress,
                        progressColor = progressColor,
                        strokeWidth = 12f
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val (h, m, s) = formatTimerDisplay(remainingSeconds)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (totalSeconds >= 3600) {
                                TimeUnit(value = h, unit = "h", color = progressColor.copy(alpha = pulseAlpha))
                                Text(":", fontFamily = OrbitronFamily, fontSize = 40.sp,
                                    color = progressColor.copy(alpha = pulseAlpha))
                            }
                            TimeUnit(value = m, unit = "m", color = progressColor.copy(alpha = pulseAlpha))
                            Text(":", fontFamily = OrbitronFamily, fontSize = 40.sp,
                                color = progressColor.copy(alpha = pulseAlpha))
                            TimeUnit(value = s, unit = "s", color = progressColor.copy(alpha = pulseAlpha))
                        }
                        if (timerState == TimerState.PAUSED) {
                            Text(
                                "Tạm dừng",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = vm::stopTimer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(52.dp)
                    ) { Text("Dừng") }

                    Button(
                        onClick = {
                            if (timerState == TimerState.RUNNING) vm.pauseTimer()
                            else vm.resumeTimer()
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(if (timerState == TimerState.RUNNING) "⏸ Tạm dừng" else "▶ Tiếp tục")
                    }
                }
            }
        }
    }
}

@Composable
fun TimeUnit(value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold, fontSize = 48.sp, color = color)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
    }
}

@Composable
fun DurationInputMode(
    digitInput: String,
    onDigit: (Int) -> Unit,
    onDoubleZero: () -> Unit,
    onDelete: () -> Unit,
    onStart: () -> Unit
) {
    Text(
        text = formatDigitInput(digitInput),
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        color = if (digitInput.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.primary,
        letterSpacing = 2.sp
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        listOf("Giờ", "Phút", "Giây").forEach {
            Text(it, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    NumPad(
        modifier = Modifier.fillMaxWidth(0.85f),
        onDigit = onDigit,
        onDoubleZero = onDoubleZero,
        onDelete = onDelete
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
        onClick = onStart,
        enabled = digitInput.isNotEmpty(),
        modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
        shape = RoundedCornerShape(50)
    ) {
        Text("▶  Bắt Đầu", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TimeRangeInputMode(
    startHour: Int, startMinute: Int,
    endHour: Int, endMinute: Int,
    onSetStart: (Int, Int) -> Unit,
    onSetEnd: (Int, Int) -> Unit,
    onStart: () -> Unit
) {
    // Calculate preview duration
    val durSecs = remember(startHour, startMinute, endHour, endMinute) {
        val start = startHour * 3600 + startMinute * 60
        var end = endHour * 3600 + endMinute * 60
        if (end <= start) end += 86400
        end - start
    }
    val h = durSecs / 3600
    val m = (durSecs % 3600) / 60

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Chọn giờ bắt đầu", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        TimePairPicker(hour = startHour, minute = startMinute, onChanged = onSetStart)

        HorizontalDivider()

        Text("Chọn giờ kết thúc", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        TimePairPicker(hour = endHour, minute = endMinute, onChanged = onSetEnd)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "⏳ Thời gian đếm ngược: ${if (h > 0) "${h}h " else ""}${m}m",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        Button(
            onClick = onStart,
            enabled = durSecs > 0,
            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("▶  Bắt Đầu", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TimePairPicker(hour: Int, minute: Int, onChanged: (Int, Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VerticalNumberPicker(value = hour, range = 0..23, onValueChange = { onChanged(it, minute) })
        Text(":", fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold,
            fontSize = 40.sp, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp))
        VerticalNumberPicker(value = minute, range = 0..59, onValueChange = { onChanged(hour, it) })
    }
}

@Composable
fun VerticalNumberPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { onValueChange(if (value >= range.last) range.first else value + 1) }) {
            Text("▲", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = "%02d".format(value),
            fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold,
            fontSize = 40.sp, color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = { onValueChange(if (value <= range.first) range.last else value - 1) }) {
            Text("▼", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun FinishedState(onReset: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "done_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scale"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⏰", fontSize = (72 * scale).sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Hết giờ rồi!",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onReset, shape = RoundedCornerShape(50),
            modifier = Modifier.height(52.dp).fillMaxWidth(0.6f)) {
            Text("Đặt lại", style = MaterialTheme.typography.titleMedium)
        }
    }
}
