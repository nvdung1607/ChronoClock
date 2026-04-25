package com.example.clock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.clock.ui.alarm.AlarmScreen
import com.example.clock.ui.clock.ClockScreen
import com.example.clock.ui.pomodoro.PomodoroScreen
import com.example.clock.ui.stopwatch.StopwatchScreen
import com.example.clock.ui.theme.*
import com.example.clock.ui.timer.TimerScreen

sealed class Screen(val route: String, val label: String, val icon: String) {
    object Clock : Screen("clock", "Đồng Hồ", "🕐")
    object Alarm : Screen("alarm", "Báo Thức", "⏰")
    object Stopwatch : Screen("stopwatch", "Bấm Giờ", "⏱️")
    object Timer : Screen("timer", "Hẹn Giờ", "⏳")
    object Pomodoro : Screen("pomodoro", "Pomodoro", "🍅")
}

val bottomNavItems = listOf(
    Screen.Clock, Screen.Alarm, Screen.Stopwatch, Screen.Timer, Screen.Pomodoro
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClockTheme(darkTheme = true) {
                ClockApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val currentScreen = bottomNavItems.find {
                currentDestination?.hierarchy?.any { d -> d.route == it.route } == true
            }
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = currentScreen?.icon ?: "🕐",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = currentScreen?.label ?: "Đồng Hồ",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Text(
                                text = screen.icon,
                                fontSize = if (selected) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Clock.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(tween(200)) { it / 4 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(150))
            }
        ) {
            composable(Screen.Clock.route) { ClockScreen() }
            composable(Screen.Alarm.route) { AlarmScreen() }
            composable(Screen.Stopwatch.route) { StopwatchScreen() }
            composable(Screen.Timer.route) { TimerScreen() }
            composable(Screen.Pomodoro.route) { PomodoroScreen() }
        }
    }
}