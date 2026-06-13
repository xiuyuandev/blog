package com.sushi.app.data.dao

import androidx.room.*
import com.sushi.app.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM Task WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task")
    suspend fun getAllTasksSync(): List<Task>

    @Query("SELECT * FROM Task WHERE isTemplate = 1")
    fun getTemplateTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE isTemplate = 1")
    suspend fun getTemplateTasksSync(): List<Task>

    @Query("SELECT * FROM Task WHERE recurrenceRule IS NOT NULL AND isCompleted = 0")
    fun getRecurringTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE linkedProfessionId = :professionId")
    fun getByProfession(professionId: String): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE linkedSkillId = :skillId AND isCompleted = 0")
    fun getActiveBySkill(skillId: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Query("SELECT * FROM Task WHERE id = :id")
    suspend fun getTaskById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("UPDATE Task SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: String)

    @Query("SELECT * FROM Task WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("DELETE FROM Task")
    suspend fun deleteAll()

    @Query("DELETE FROM Task WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM Task WHERE linkedSkillId = :skillId AND isCompleted = 0")
    suspend fun deleteActiveBySkillId(skillId: String)

    @Query("UPDATE Task SET isCompleted = 0 WHERE id = :id")
    suspend fun reactivate(id: String)
}
