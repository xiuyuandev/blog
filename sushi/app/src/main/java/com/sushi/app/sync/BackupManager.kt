package com.sushi.app.sync

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sushi.app.data.model.*
import com.sushi.app.data.repository.SushiRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 备份数据结构
 */
data class BackupData(
    val version: Int = 1,
    val exportTime: Long = System.currentTimeMillis(),
    val skills: List<Skill>,
    val professions: List<Profession>,
    val affixes: List<Affix>,
    val tasks: List<Task>,
    val timeRecords: List<TimeRecord>
)

/**
 * 备份管理器：负责 JSON 导出/导入
 */
@Singleton
class BackupManager @Inject constructor(
    private val repository: SushiRepository
) {
    private val gson = Gson()

    /**
     * 导出所有数据为 JSON 字符串
     */
    suspend fun exportToJson(): String {
        val skills = repository.getAllSkills().first()
        val professions = repository.getAllProfessions().first()
        val affixes = repository.getAllAffixes().first()
        val tasks = repository.getAllTasks().first()
        val timeRecords = repository.getAllRecords().first()

        val backup = BackupData(
            skills = skills,
            professions = professions,
            affixes = affixes,
            tasks = tasks,
            timeRecords = timeRecords
        )

        return gson.toJson(backup)
    }

    /**
     * 从 JSON 字符串导入数据（覆盖现有数据）
     */
    suspend fun importFromJson(json: String): Result<Unit> {
        return try {
            val backup = gson.fromJson(json, BackupData::class.java)

            // 批量插入，使用 REPLACE 策略覆盖
            repository.insertSkills(backup.skills)
            if (backup.professions.isNotEmpty()) repository.insertProfessions(backup.professions)
            if (backup.affixes.isNotEmpty()) repository.insertAffixes(backup.affixes)
            backup.tasks.forEach { repository.insertTask(it) }
            backup.timeRecords.forEach { repository.insertTimeRecord(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 验证 JSON 格式是否合法
     */
    fun validateJson(json: String): Boolean {
        return try {
            val backup = gson.fromJson(json, BackupData::class.java)
            backup.version > 0
        } catch (_: Exception) {
            false
        }
    }
}
