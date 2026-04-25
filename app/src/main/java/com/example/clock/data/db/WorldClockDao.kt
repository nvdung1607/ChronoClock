package com.example.clock.data.db

import androidx.room.*
import com.example.clock.data.model.WorldClock
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {
    @Query("SELECT * FROM world_clocks ORDER BY sortOrder, cityName")
    fun getAllWorldClocks(): Flow<List<WorldClock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorldClock(worldClock: WorldClock)

    @Delete
    suspend fun deleteWorldClock(worldClock: WorldClock)

    @Query("UPDATE world_clocks SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Int, order: Int)
}
