package com.example.clock.ui.stopwatch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clock.ui.components.CircularProgressArc
import com.example.clock.ui.theme.*

@Composable
fun StopwatchScreen(vm: StopwatchViewModel = viewModel()) {
    val elapsedMs by vm.elapsedMs.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val laps by vm.laps.collectAsState()

    val progress = ((elapsedMs % 60000) / 60000f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Clock face with arc
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressArc(
                modifier = Modifier.fillMaxSize(),
                progress = progress,
                progressColor = if (isRunning) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                strokeWidth = 10f
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatStopwatchTime(elapsedMs),
                    fontFamily = OrbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (elapsedMs >= 3600000) 32.sp else 40.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
                if (laps.isNotEmpty()) {
                    Text(
                        text = "Vòng ${laps.size + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset / Lap button
            if (elapsedMs > 0) {
                OutlinedButton(
                    onClick = { if (isRunning) vm.lap() else vm.reset() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isRunning) "Vòng" else "Reset",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            // Start / Pause
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { if (isRunning) vm.pause() else vm.start() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = if (isRunning) "⏸" else "▶",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.size(72.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lap list
        if (laps.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(laps) { index, lap ->
                    val isFastest = index == vm.fastestLapIndex && laps.size >= 2
                    val isSlowest = index == vm.slowestLapIndex && laps.size >= 2
                    LapRow(
                        lap = lap,
                        isFastest = isFastest,
                        isSlowest = isSlowest
                    )
                }
            }
        }
    }
}

@Composable
fun LapRow(lap: LapEntry, isFastest: Boolean, isSlowest: Boolean) {
    val textColor = when {
        isFastest -> MaterialTheme.colorScheme.tertiary
        isSlowest -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFastest -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    isSlowest -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Vòng ${lap.lapNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            if (isFastest) {
                Spacer(modifier = Modifier.width(6.dp))
                Text("🏆", fontSize = 12.sp)
            }
            if (isSlowest) {
                Spacer(modifier = Modifier.width(6.dp))
                Text("🐢", fontSize = 12.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatLapTime(lap.lapTimeMs),
                fontFamily = OrbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Text(
                text = formatStopwatchTime(lap.totalTimeMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
