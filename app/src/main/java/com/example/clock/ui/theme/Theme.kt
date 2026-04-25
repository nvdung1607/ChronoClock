package com.example.clock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = BackgroundDeep,
    primaryContainer = CyanDim,
    onPrimaryContainer = TextPrimary,
    secondary = PurpleElectric,
    onSecondary = TextPrimary,
    secondaryContainer = PurpleDim,
    onSecondaryContainer = TextPrimary,
    tertiary = GreenNeon,
    onTertiary = BackgroundDeep,
    background = BackgroundDeep,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary,
    outline = TextTertiary,
    outlineVariant = SurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = CyanLight,
    onPrimary = Color.White,
    primaryContainer = CardLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = PurpleLight,
    onSecondary = Color.White,
    secondaryContainer = BackgroundLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = Color(0xFF0E8B5E),
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorRed,
    onError = Color.White,
    outline = TextSecondaryLight,
)

@Composable
fun ClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}