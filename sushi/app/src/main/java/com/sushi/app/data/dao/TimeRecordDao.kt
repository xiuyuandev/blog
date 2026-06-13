package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.TimeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRecordDao {
    @Query("SELECT * FROM TimeRecord ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    fun getRecordsByDate(startOfDay: Long, endOfDay: Long): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getRecordsByTaskId(taskId: String): Flow<List<TimeRecord>>

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalNetDuration(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord")
    suspend fun getTotalNetDurationAll(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TimeRecord)

    @Query("SELECT COUNT(*) FROM TimeRecord")
    suspend fun getCount(): Int
}
