package com.example.clock.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.clock.data.model.Alarm
import com.example.clock.data.model.TimerPreset
import com.example.clock.data.model.WorldClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Alarm::class, WorldClock::class, TimerPreset::class],
    version = 1,
    exportSchema = false
)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun worldClockDao(): WorldClockDao
    abstract fun timerPresetDao(): TimerPresetDao

    companion object {
        @Volatile
        private var INSTANCE: ClockDatabase? = null

        fun getInstance(context: Context): ClockDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ClockDatabase::class.java,
                    "clock_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default timer presets
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.timerPresetDao().insertPreset(
                                        TimerPreset(label = "Trà xanh", durationSeconds = 180, emoji = "🍵")
                                    )
                                    database.timerPresetDao().insertPreset(
                                        TimerPreset(label = "Cà phê", durationSeconds = 240, emoji = "☕")
                                    )
                                    database.timerPresetDao().insertPreset(
                                        TimerPreset(label = "Nấu mì", durationSeconds = 300, emoji = "🍜")
                                    )
                                    database.timerPresetDao().insertPreset(
                                        TimerPreset(label = "Nghỉ ngơi", durationSeconds = 600, emoji = "😴")
                                    )
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
