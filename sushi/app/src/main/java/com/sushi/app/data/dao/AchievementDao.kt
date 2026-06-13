package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM Achievement ORDER BY isUnlocked DESC, threshold ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM Achievement")
    suspend fun getAllAchievementsSync(): List<Achievement>

    @Query("SELECT * FROM Achievement WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Query("SELECT * FROM Achievement WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT COUNT(*) FROM Achievement WHERE isUnlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<Achievement>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: Achievement)

    @Update
    suspend fun update(achievement: Achievement)

    @Query("UPDATE Achievement SET currentProgress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("UPDATE Achievement SET isUnlocked = 1, unlockedAt = :unlockedAt WHERE id = :id")
    suspend fun markUnlocked(id: String, unlockedAt: Long)

    @Query("DELETE FROM Achievement")
    suspend fun deleteAll()
}
