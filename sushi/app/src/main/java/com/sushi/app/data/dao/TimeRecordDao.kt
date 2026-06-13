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

    @Query("SELECT * FROM TimeRecord WHERE skillId = :skillId ORDER BY timestamp DESC")
    fun getRecordsBySkillId(skillId: String): Flow<List<TimeRecord>>

    @Query("SELECT * FROM TimeRecord WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getRecordsByTaskId(taskId: String): Flow<List<TimeRecord>>

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord WHERE startDateTime BETWEEN :startTime AND :endTime")
    suspend fun getTotalNetDuration(startTime: Long, endTime: Long): Int?

    @Query("SELECT SUM(netDurationMin) FROM TimeRecord")
    suspend fun getTotalNetDurationAll(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TimeRecord)

    @Query("SELECT COUNT(*) FROM TimeRecord")
    suspend fun getCount(): Int

    @Query("DELETE FROM TimeRecord")
    suspend fun deleteAll()

    @Query("DELETE FROM TimeRecord WHERE id = :id")
    suspend fun deleteById(id: String)

    // Fix #3: 删除技能时清理关联的 TimeRecord
    @Query("DELETE FROM TimeRecord WHERE skillId = :skillId")
    suspend fun deleteBySkillId(skillId: String)

    @Update
    suspend fun update(record: TimeRecord)

    @Query("DELETE FROM TimeRecord WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}
