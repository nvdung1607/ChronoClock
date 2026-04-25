package com.example.clock.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CircularProgressArc(
    modifier: Modifier = Modifier,
    progress: Float,          // 0f to 1f
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    progressColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 12f,
    startAngle: Float = -90f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress_arc"
    )

    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height)
        val topLeft = Offset(
            (size.width - diameter) / 2 + strokeWidth / 2,
            (size.height - diameter) / 2 + strokeWidth / 2
        )
        val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)

        // Track
        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress
        if (animatedProgress > 0f) {
            drawArc(
                color = progressColor,
                startAngle = startAngle,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
