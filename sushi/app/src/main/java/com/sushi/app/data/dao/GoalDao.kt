package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM Goal WHERE isActive = 1 ORDER BY endTime ASC")
    fun getActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM Goal ORDER BY endTime DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM Goal")
    suspend fun getAllGoalsSync(): List<Goal>

    @Query("SELECT * FROM Goal WHERE id = :id")
    suspend fun getGoalById(id: String): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<Goal>)

    @Update
    suspend fun update(goal: Goal)

    @Query("UPDATE Goal SET currentMinutes = :currentMinutes WHERE id = :id")
    suspend fun updateProgress(id: String, currentMinutes: Int)

    @Query("UPDATE Goal SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("DELETE FROM Goal WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM Goal")
    suspend fun deleteAll()
}
