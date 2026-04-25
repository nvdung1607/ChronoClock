package com.example.clock.ui.alarm

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clock.data.model.Alarm
import com.example.clock.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen() {
    val context = LocalContext.current
    val vm: AlarmViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(context) as T
        }
    })
    val alarms by vm.alarms.collectAsState()
    var showAddEdit by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⏰", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Chưa có báo thức nào",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Nhấn + để thêm báo thức mới",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        timeUntil = if (alarm.isEnabled) vm.getTimeUntilAlarm(alarm) else "",
                        onToggle = { vm.toggleAlarm(alarm, it) },
                        onEdit = { editingAlarm = alarm; showAddEdit = true },
                        onDelete = { vm.deleteAlarm(alarm) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { editingAlarm = null; showAddEdit = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm báo thức")
        }
    }

    if (showAddEdit) {
        AlarmEditSheet(
            alarm = editingAlarm,
            onSave = { alarm ->
                if (editingAlarm == null) vm.addAlarm(alarm)
                else vm.updateAlarm(alarm.copy(id = editingAlarm!!.id))
                showAddEdit = false
            },
            onDismiss = { showAddEdit = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCard(
    alarm: Alarm,
    timeUntil: String,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val repeatDays = if (alarm.repeatDays.isNotEmpty())
        alarm.repeatDays.split(",").map { it.trim().toInt() }
    else emptyList()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onEdit() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (alarm.isEnabled)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%02d:%02d".format(alarm.hour, alarm.minute),
                            fontFamily = OrbitronFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    if (alarm.label.isNotEmpty()) {
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (timeUntil.isNotEmpty()) {
                        Text(
                            text = timeUntil,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }

                    if (repeatDays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            dayNames.forEachIndexed { index, day ->
                                val isActive = (index + 1) in repeatDays
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    } else if (alarm.repeatDays.isEmpty()) {
                        Text(
                            text = "Một lần",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditSheet(
    alarm: Alarm?,
    onSave: (Alarm) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(alarm?.hour ?: 7) }
    var minute by remember { mutableStateOf(alarm?.minute ?: 0) }
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var isVibrate by remember { mutableStateOf(alarm?.isVibrate ?: true) }
    var snoozeMinutes by remember { mutableStateOf(alarm?.snoozeMinutes ?: 5) }
    val dayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    var selectedDays by remember {
        mutableStateOf(
            if (alarm?.repeatDays?.isNotEmpty() == true)
                alarm.repeatDays.split(",").map { it.trim().toInt() }.toSet()
            else emptySet()
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            Text(
                text = if (alarm == null) "Báo Thức Mới" else "Chỉnh Sửa Báo Thức",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Time picker (simple number pickers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(value = hour, range = 0..23, onValueChange = { hour = it })
                Text(
                    ":", fontFamily = OrbitronFamily, fontWeight = FontWeight.Bold,
                    fontSize = 48.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                NumberPicker(value = minute, range = 0..59, onValueChange = { minute = it })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Nhãn báo thức") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat days
            Text("Lặp lại", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dayNames.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    val isSelected = dayNum in selectedDays
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedDays = if (isSelected) selectedDays - dayNum
                            else selectedDays + dayNum
                        },
                        label = { Text(day, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vibrate toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📳 Rung", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isVibrate, onCheckedChange = { isVibrate = it })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Snooze
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏸️ Báo lại sau (phút)", style = MaterialTheme.typography.bodyLarge)
                SegmentedSnooze(
                    selected = snoozeMinutes,
                    options = listOf(5, 10, 15),
                    onSelect = { snoozeMinutes = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSave(
                        Alarm(
                            hour = hour, minute = minute,
                            label = label,
                            isEnabled = true,
                            repeatDays = selectedDays.sorted().joinToString(","),
                            isVibrate = isVibrate,
                            snoozeMinutes = snoozeMinutes
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Lưu Báo Thức", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NumberPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            onValueChange(if (value >= range.last) range.first else value + 1)
        }) {
            Text("▲", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = "%02d".format(value),
            fontFamily = OrbitronFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = {
            onValueChange(if (value <= range.first) range.last else value - 1)
        }) {
            Text("▼", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SegmentedSnooze(selected: Int, options: List<Int>, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { opt ->
            val isSelected = opt == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(opt) },
                label = { Text("${opt}m", style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
