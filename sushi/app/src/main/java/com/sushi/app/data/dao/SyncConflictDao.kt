package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.SyncConflict
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncConflictDao {
    @Query("SELECT * FROM SyncConflict WHERE resolution = 'PENDING'")
    fun getPendingConflicts(): Flow<List<SyncConflict>>

    @Query("SELECT * FROM SyncConflict")
    suspend fun getAllSync(): List<SyncConflict>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conflict: SyncConflict)

    @Query("UPDATE SyncConflict SET resolution = :resolution WHERE id = :id")
    suspend fun updateResolution(id: String, resolution: String)

    @Query("DELETE FROM SyncConflict WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM SyncConflict")
    suspend fun deleteAll()
}
