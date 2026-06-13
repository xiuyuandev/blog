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
     * Fix #5: 真正的"覆盖"——先清空所有表，再插入备份数据
     */
    suspend fun importFromJson(json: String): Result<Unit> {
        return try {
            val backup = gson.fromJson(json, BackupData::class.java)
                ?: return Result.failure(IllegalArgumentException("无效的备份数据"))

            // 真正清空后插入
            repository.clearAllTables()
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

    /**
     * 智能合并：将云端数据与本地数据合并。
     * 相同 ID 的实体，根据策略选择保留哪个版本。
     * Skills/Professions：保留 totalExp 更高的。
     * Tasks：已完成的优先。
     * TimeRecords：保留 timestamp 更新的。
     * Affixes：云端版本优先。
     */
    suspend fun mergeFromJson(json: String) {
        val cloudData = gson.fromJson(json, BackupData::class.java)
            ?: throw IllegalArgumentException("Invalid backup format")

        val localSkills = repository.getAllSkillsSync()
        val localProfessions = repository.getAllProfessionsSync()
        val localAffixes = repository.getAllAffixesSync()
        val localTasks = repository.getAllTasksSync()
        val localRecords = repository.getAllRecordsSync()

        val mergedSkills = mergeById(
            localSkills, cloudData.skills, { it.id },
            { local, cloud -> if (cloud.totalExp >= local.totalExp) cloud else local }
        )
        val mergedProfessions = mergeById(
            localProfessions, cloudData.professions, { it.id },
            { local, cloud -> if (cloud.totalExp >= local.totalExp) cloud else local }
        )
        val mergedAffixes = mergeById(
            localAffixes, cloudData.affixes, { it.id },
            { _, cloud -> cloud }
        )
        val mergedTasks = mergeById(
            localTasks, cloudData.tasks, { it.id },
            { local, cloud -> if (cloud.isCompleted && !local.isCompleted) cloud else local }
        )
        val mergedRecords = mergeById(
            localRecords, cloudData.timeRecords, { it.id },
            { local, cloud -> if (cloud.timestamp >= local.timestamp) cloud else local }
        )

        repository.insertSkills(mergedSkills)
        repository.insertProfessions(mergedProfessions)
        repository.insertAffixes(mergedAffixes)
        repository.insertAllTasks(mergedTasks)
        repository.insertAllTimeRecords(mergedRecords)
    }

    /**
     * 按 ID 合并两个列表，冲突时使用 resolve 函数决定保留哪个。
     */
    private fun <T> mergeById(
        localList: List<T>,
        cloudList: List<T>,
        getId: (T) -> String,
        resolve: (local: T, cloud: T) -> T
    ): List<T> {
        val localMap = localList.associateBy(getId)
        val cloudMap = cloudList.associateBy(getId)
        val allIds = localMap.keys + cloudMap.keys
        return allIds.mapNotNull { id ->
            val local = localMap[id]
            val cloud = cloudMap[id]
            when {
                local != null && cloud != null -> resolve(local, cloud)
                local != null -> local
                cloud != null -> cloud
                else -> null
            }
        }
    }

    /**
     * 版本迁移：根据备份数据版本号进行迁移。
     */
    fun migrateBackup(data: BackupData): BackupData {
        return when (data.version) {
            1 -> data // 当前版本，无需迁移
            else -> data
        }
    }
}
