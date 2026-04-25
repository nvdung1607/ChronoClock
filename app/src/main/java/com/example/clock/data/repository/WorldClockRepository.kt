package com.example.clock.data.repository

import com.example.clock.data.db.WorldClockDao
import com.example.clock.data.model.WorldClock
import kotlinx.coroutines.flow.Flow

class WorldClockRepository(private val dao: WorldClockDao) {
    fun getAllWorldClocks(): Flow<List<WorldClock>> = dao.getAllWorldClocks()
    suspend fun insertWorldClock(worldClock: WorldClock) = dao.insertWorldClock(worldClock)
    suspend fun deleteWorldClock(worldClock: WorldClock) = dao.deleteWorldClock(worldClock)
}

// Available world timezones to add
data class AvailableCity(
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val flag: String
)

val ALL_CITIES = listOf(
    AvailableCity("Hà Nội", "Việt Nam", "Asia/Ho_Chi_Minh", "🇻🇳"),
    AvailableCity("TP. Hồ Chí Minh", "Việt Nam", "Asia/Ho_Chi_Minh", "🇻🇳"),
    AvailableCity("New York", "USA", "America/New_York", "🇺🇸"),
    AvailableCity("Los Angeles", "USA", "America/Los_Angeles", "🇺🇸"),
    AvailableCity("Chicago", "USA", "America/Chicago", "🇺🇸"),
    AvailableCity("London", "UK", "Europe/London", "🇬🇧"),
    AvailableCity("Paris", "France", "Europe/Paris", "🇫🇷"),
    AvailableCity("Berlin", "Germany", "Europe/Berlin", "🇩🇪"),
    AvailableCity("Tokyo", "Japan", "Asia/Tokyo", "🇯🇵"),
    AvailableCity("Seoul", "South Korea", "Asia/Seoul", "🇰🇷"),
    AvailableCity("Beijing", "China", "Asia/Shanghai", "🇨🇳"),
    AvailableCity("Shanghai", "China", "Asia/Shanghai", "🇨🇳"),
    AvailableCity("Singapore", "Singapore", "Asia/Singapore", "🇸🇬"),
    AvailableCity("Bangkok", "Thailand", "Asia/Bangkok", "🇹🇭"),
    AvailableCity("Jakarta", "Indonesia", "Asia/Jakarta", "🇮🇩"),
    AvailableCity("Mumbai", "India", "Asia/Kolkata", "🇮🇳"),
    AvailableCity("Delhi", "India", "Asia/Kolkata", "🇮🇳"),
    AvailableCity("Dubai", "UAE", "Asia/Dubai", "🇦🇪"),
    AvailableCity("Moscow", "Russia", "Europe/Moscow", "🇷🇺"),
    AvailableCity("Sydney", "Australia", "Australia/Sydney", "🇦🇺"),
    AvailableCity("Auckland", "New Zealand", "Pacific/Auckland", "🇳🇿"),
    AvailableCity("Toronto", "Canada", "America/Toronto", "🇨🇦"),
    AvailableCity("São Paulo", "Brazil", "America/Sao_Paulo", "🇧🇷"),
    AvailableCity("Cairo", "Egypt", "Africa/Cairo", "🇪🇬"),
    AvailableCity("Johannesburg", "South Africa", "Africa/Johannesburg", "🇿🇦"),
)
