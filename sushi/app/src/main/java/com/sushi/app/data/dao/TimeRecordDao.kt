package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.TimeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRecordDao {
    @Query("SELECT * FROM TimeRecord ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord")
    suspend fun getAllRecordsSync(): List<TimeRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<TimeRecord>)

    @Query("SELECT * FROM TimeRecord WHERE startDateTime BETWEEN :startOfDay AND :endOfDay ORDER BY startDateTime ASC")
    fun getRecordsByDate(startOfDay: Long, endOfDay: Long): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord WHERE attributionDateKey = :dateKey ORDER BY startDateTime ASC")
    fun getRecordsByDateKey(dateKey: String): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord WHERE skillId = :skillId ORDER BY timestamp DESC")
    fun getRecordsBySkillId(skillId: String): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getRecordsByTaskId(taskId: String): Flow<List<TimeRecord>>

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord WHERE startDateTime BETWEEN :startTime AND :endTime")
    suspend fun getTotalNetDuration(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord WHERE attributionDateKey = :dateKey")
    suspend fun getTotalNetDurationByDateKey(dateKey: String): Int?

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord")
    suspend fun getTotalNetDurationAll(): Int?

    @Query("SELECT attributionDateKey, SUM(netDurationMin) as totalMin FROM TimeRecord GROUP BY attributionDateKey")
    suspend fun getDailyTotalsRaw(): List<DailyTotalRaw>

    @Query("SELECT * FROM TimeRecord WHERE startDateTime >= :startTime ORDER BY startDateTime ASC")
    suspend fun getRecordsSince(startTime: Long): List<TimeRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TimeRecord)

    @Query("SELECT COUNT(*) FROM TimeRecord")
    suspend fun getCount(): Int

    @Query("DELETE FROM TimeRecord")
    suspend fun deleteAll()

    @Query("DELETE FROM TimeRecord WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM TimeRecord WHERE skillId = :skillId")
    suspend fun deleteBySkillId(skillId: String)

    @Update
    suspend fun update(record: TimeRecord)

    @Query("DELETE FROM TimeRecord WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}

data class DailyTotalRaw(
    val attributionDateKey: String,
    val totalMin: Int
)
