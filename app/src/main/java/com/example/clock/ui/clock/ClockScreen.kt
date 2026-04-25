package com.example.clock.ui.clock

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clock.data.model.WorldClock
import com.example.clock.data.repository.ALL_CITIES
import com.example.clock.data.repository.AvailableCity
import com.example.clock.ui.components.AnalogClock
import com.example.clock.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen() {
    val context = LocalContext.current
    val vm: ClockViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ClockViewModel(context) as T
        }
    })

    val worldClocks by vm.worldClocks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    // Tick every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                LocalClockHeader(currentTime = currentTime)
            }

            if (worldClocks.isNotEmpty()) {
                item {
                    Text(
                        text = "Đồng Hồ Thế Giới",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                items(worldClocks, key = { it.id }) { worldClock ->
                    WorldClockCard(
                        worldClock = worldClock,
                        onDelete = { vm.deleteWorldClock(worldClock) }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm múi giờ")
        }
    }

    if (showAddDialog) {
        AddWorldClockDialog(
            existingIds = worldClocks.map { it.timeZoneId }.toSet(),
            onAdd = { city ->
                vm.addWorldClock(
                    WorldClock(
                        cityName = city.cityName,
                        countryName = city.countryName,
                        timeZoneId = city.timeZoneId,
                        sortOrder = worldClocks.size
                    )
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun LocalClockHeader(currentTime: Calendar) {
    val timeStr = remember(currentTime) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime.time)
    }
    val secStr = remember(currentTime) {
        SimpleDateFormat(":ss", Locale.getDefault()).format(currentTime.time)
    }
    val dateStr = remember(currentTime) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi")).format(currentTime.time)
    }
    val tzName = remember {
        val tz = TimeZone.getDefault()
        tz.getDisplayName(false, TimeZone.SHORT)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Analog Clock
        AnalogClock(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Digital time
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = timeStr,
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = secStr,
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = tzName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockCard(
    worldClock: WorldClock,
    onDelete: () -> Unit
) {
    var timeStr by remember { mutableStateOf(getTimeForTimezone(worldClock.timeZoneId)) }
    var dateStr by remember { mutableStateOf(getDateForTimezone(worldClock.timeZoneId)) }
    val isDaytime = remember(timeStr) { isDaytime(worldClock.timeZoneId) }

    LaunchedEffect(worldClock.timeZoneId) {
        while (true) {
            timeStr = getTimeForTimezone(worldClock.timeZoneId)
            dateStr = getDateForTimezone(worldClock.timeZoneId)
            delay(1000)
        }
    }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day/Night indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDaytime)
                                Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00)))
                            else
                                Brush.radialGradient(listOf(Color(0xFF6B8CFF), Color(0xFF1A1A6E)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isDaytime) "☀️" else "🌙",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = worldClock.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${worldClock.countryName} · ${getGmtOffset(worldClock.timeZoneId)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = timeStr,
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorldClockDialog(
    existingIds: Set<String>,
    onAdd: (AvailableCity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        ALL_CITIES.filter {
            (it.cityName.contains(searchQuery, ignoreCase = true) ||
                    it.countryName.contains(searchQuery, ignoreCase = true)) &&
                    !existingIds.contains(it.timeZoneId)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Múi Giờ") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm thành phố...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered) { city ->
                        ListItem(
                            headlineContent = {
                                Text("${city.flag} ${city.cityName}")
                            },
                            supportingContent = {
                                Text("${city.countryName} · ${getGmtOffset(city.timeZoneId)}")
                            },
                            modifier = Modifier
                                .clickable { onAdd(city) }
                                .clip(RoundedCornerShape(8.dp))
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}
