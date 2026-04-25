package com.example.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    showSecondHand: Boolean = true
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val dialColor = MaterialTheme.colorScheme.surfaceVariant
    val dotColor = MaterialTheme.colorScheme.primary

    val time by produceState(initialValue = Calendar.getInstance()) {
        while (true) {
            value = Calendar.getInstance()
            kotlinx.coroutines.delay(50)
        }
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2f * 0.9f

        // Dial background
        drawCircle(color = dialColor, radius = radius, center = center)
        drawCircle(
            color = primaryColor.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        // Hour marks
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val isMainMark = i % 3 == 0
            val markLength = if (isMainMark) radius * 0.15f else radius * 0.08f
            val markWidth = if (isMainMark) 3f else 1.5f
            val startX = center.x + (radius * 0.82f) * cos(angle).toFloat()
            val startY = center.y + (radius * 0.82f) * sin(angle).toFloat()
            val endX = center.x + (radius * 0.82f + markLength) * cos(angle).toFloat()
            val endY = center.y + (radius * 0.82f + markLength) * sin(angle).toFloat()
            drawLine(
                color = if (isMainMark) dotColor else dotColor.copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = markWidth,
                cap = StrokeCap.Round
            )
        }

        val cal = time
        val hour = cal.get(Calendar.HOUR).toFloat()
        val minute = cal.get(Calendar.MINUTE).toFloat()
        val second = cal.get(Calendar.SECOND).toFloat()
        val millis = cal.get(Calendar.MILLISECOND).toFloat()

        val secondAngle = Math.toRadians(((second + millis / 1000f) * 6f - 90).toDouble())
        val minuteAngle = Math.toRadians(((minute + second / 60f) * 6f - 90).toDouble())
        val hourAngle = Math.toRadians(((hour + minute / 60f) * 30f - 90).toDouble())

        // Hour hand
        drawHand(center, radius * 0.5f, hourAngle, primaryColor, 8f)
        // Minute hand
        drawHand(center, radius * 0.72f, minuteAngle, primaryColor, 5f)
        // Second hand
        if (showSecondHand) {
            drawHand(center, radius * 0.82f, secondAngle, secondaryColor, 2f)
            // Second tail
            val tailX = center.x + radius * 0.2f * cos(secondAngle + Math.PI).toFloat()
            val tailY = center.y + radius * 0.2f * sin(secondAngle + Math.PI).toFloat()
            drawLine(
                color = secondaryColor,
                start = center,
                end = Offset(tailX, tailY),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Center dot
        drawCircle(color = dotColor, radius = 8f, center = center)
        drawCircle(color = dialColor, radius = 4f, center = center)
    }
}

private fun DrawScope.drawHand(
    center: Offset,
    length: Float,
    angle: Double,
    color: Color,
    width: Float
) {
    val endX = center.x + length * cos(angle).toFloat()
    val endY = center.y + length * sin(angle).toFloat()
    drawLine(
        color = color,
        start = center,
        end = Offset(endX, endY),
        strokeWidth = width,
        cap = StrokeCap.Round
    )
}
