package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.DailyReflection
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReflectionDao {
    @Query("SELECT * FROM DailyReflection ORDER BY dateKey DESC")
    fun getAllReflections(): Flow<List<DailyReflection>>

    @Query("SELECT * FROM DailyReflection WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getReflectionByDate(dateKey: String): DailyReflection?

    @Query("SELECT * FROM DailyReflection WHERE dateKey BETWEEN :startKey AND :endKey ORDER BY dateKey DESC")
    fun getReflectionsInRange(startKey: String, endKey: String): Flow<List<DailyReflection>>

    @Query("SELECT * FROM DailyReflection")
    suspend fun getAllReflectionsSync(): List<DailyReflection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reflection: DailyReflection)

    @Update
    suspend fun update(reflection: DailyReflection)

    @Query("DELETE FROM DailyReflection WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM DailyReflection")
    suspend fun deleteAll()
}
