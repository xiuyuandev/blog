package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.HelpEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HelpEntryDao {
    @Query("SELECT * FROM HelpEntry ORDER BY sortOrder ASC")
    fun getAllEntries(): Flow<List<HelpEntry>>

    @Query("SELECT * FROM HelpEntry WHERE category = :category ORDER BY sortOrder ASC")
    fun getByCategory(category: String): Flow<List<HelpEntry>>

    @Query("SELECT * FROM HelpEntry")
    suspend fun getAllSync(): List<HelpEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<HelpEntry>)

    @Query("DELETE FROM HelpEntry")
    suspend fun deleteAll()
}
