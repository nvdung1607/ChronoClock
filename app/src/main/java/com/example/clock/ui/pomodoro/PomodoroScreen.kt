package com.example.clock.ui.pomodoro

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
import com.example.clock.ui.theme.*

@Composable
fun PomodoroScreen(vm: PomodoroViewModel = viewModel()) {
    val phase by vm.phase.collectAsState()
    val remainingSeconds by vm.remainingSeconds.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val completedSessions by vm.completedSessions.collectAsState()
    val todayPomodoros by vm.todayPomodoros.collectAsState()
    val settings by vm.settings.collectAsState()
    val progress by vm.progressFlow.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    val phaseColor = when (phase) {
        PomodoroPhase.FOCUS -> MaterialTheme.colorScheme.primary
        PomodoroPhase.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
        PomodoroPhase.LONG_BREAK -> MaterialTheme.colorScheme.secondary
    }
    val phaseName = when (phase) {
        PomodoroPhase.FOCUS -> "🍅 Tập trung"
        PomodoroPhase.SHORT_BREAK -> "☕ Nghỉ ngắn"
        PomodoroPhase.LONG_BREAK -> "😴 Nghỉ dài"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pomodoro",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { showSettings = true }) {
                Text("⚙️", fontSize = 24.sp)
            }
        }

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("🎯 Hôm nay", "$todayPomodoros 🍅", Modifier.weight(1f))
            StatCard("✅ Phiên", "$completedSessions", Modifier.weight(1f))
            StatCard("⏱ Focus", "${settings.focusMinutes}m", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phase label
        AnimatedContent(
            targetState = phaseName,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "phase_label"
        ) { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = phaseColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Session dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sessionsInCycle = completedSessions % settings.sessionsBeforeLongBreak
            repeat(settings.sessionsBeforeLongBreak) { index ->
                val filled = index < sessionsInCycle
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (filled) phaseColor else phaseColor.copy(alpha = 0.25f))
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Circular timer
        Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            CircularProgressArc(
                modifier = Modifier.fillMaxSize(),
                progress = progress,
                progressColor = phaseColor,
                trackColor = phaseColor.copy(alpha = 0.15f),
                strokeWidth = 14f
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val m = remainingSeconds / 60
                val s = remainingSeconds % 60
                Text(
                    text = "%02d:%02d".format(m, s),
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 56.sp,
                    color = phaseColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = vm::reset,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp)
            ) { Text("Reset") }

            Button(
                onClick = vm::toggle,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp).width(140.dp),
                colors = ButtonDefaults.buttonColors(containerColor = phaseColor)
            ) {
                Text(
                    text = if (isRunning) "⏸ Dừng" else "▶ Bắt đầu",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            OutlinedButton(
                onClick = vm::skip,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp)
            ) { Text("⏭ Bỏ qua") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Phase descriptions
        PhaseInfoCard(phase = phase, settings = settings)
    }

    if (showSettings) {
        PomodoroSettingsSheet(
            settings = settings,
            onSave = { vm.updateSettings(it); showSettings = false },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PhaseInfoCard(phase: PomodoroPhase, settings: PomodoroSettings) {
    val (emoji, title, desc) = when (phase) {
        PomodoroPhase.FOCUS -> Triple(
            "🎯", "Chế độ tập trung",
            "Tắt điện thoại, loại bỏ phiền nhiễu\nvà tập trung làm việc trong ${settings.focusMinutes} phút."
        )
        PomodoroPhase.SHORT_BREAK -> Triple(
            "☕", "Nghỉ ngắn",
            "Đứng dậy, vươn vai, uống nước.\nNghỉ ${settings.shortBreakMinutes} phút để lấy lại sức."
        )
        PomodoroPhase.LONG_BREAK -> Triple(
            "😴", "Nghỉ dài",
            "Bạn đã làm tốt! Nghỉ ngơi thật sự\ntrong ${settings.longBreakMinutes} phút."
        )
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsSheet(
    settings: PomodoroSettings,
    onSave: (PomodoroSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var focus by remember { mutableStateOf(settings.focusMinutes) }
    var shortBreak by remember { mutableStateOf(settings.shortBreakMinutes) }
    var longBreak by remember { mutableStateOf(settings.longBreakMinutes) }
    var sessions by remember { mutableStateOf(settings.sessionsBeforeLongBreak) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Text("⚙️ Cài đặt Pomodoro", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp))

            SettingSlider("🎯 Tập trung", focus, 1..60, { focus = it })
            SettingSlider("☕ Nghỉ ngắn", shortBreak, 1..30, { shortBreak = it })
            SettingSlider("😴 Nghỉ dài", longBreak, 5..60, { longBreak = it })
            SettingSlider("🔄 Phiên trước khi nghỉ dài", sessions, 2..8, { sessions = it })

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onSave(PomodoroSettings(focus, shortBreak, longBreak, sessions)) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Lưu cài đặt") }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingSlider(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value}m", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}
