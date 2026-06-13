package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.PauseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PauseLogDao {
    @Query("SELECT * FROM PauseLog ORDER BY pausedAt DESC")
    fun getAllPauseLogs(): Flow<List<PauseLog>>

    @Query("SELECT * FROM PauseLog WHERE timeRecordId = :recordId ORDER BY pausedAt ASC")
    fun getPauseLogsByRecord(recordId: String): Flow<List<PauseLog>>

    @Query("SELECT * FROM PauseLog")
    suspend fun getAllPauseLogsSync(): List<PauseLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: PauseLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<PauseLog>)

    @Query("DELETE FROM PauseLog WHERE timeRecordId = :recordId")
    suspend fun deleteByRecordId(recordId: String)

    @Query("DELETE FROM PauseLog")
    suspend fun deleteAll()
}
