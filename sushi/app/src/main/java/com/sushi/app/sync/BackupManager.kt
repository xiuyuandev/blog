package com.sushi.app.sync

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sushi.app.data.model.*
import com.sushi.app.data.repository.SushiRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 备份数据结构
 */
data class BackupData(
    val version: Int = 2,
    val exportTime: Long = System.currentTimeMillis(),
    val skills: List<Skill>,
    val professions: List<Profession>,
    val affixes: List<Affix>,
    val tasks: List<Task>,
    val timeRecords: List<TimeRecord>,
    val achievements: List<Achievement> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val reflections: List<DailyReflection> = emptyList()
)

/**
 * 备份管理器:负责 JSON/CSV 导出/导入
 *
 * V1.0 由 [com.sushi.app.SushiContainer] 持有单例,不再依赖 Hilt。
 * 同时支持本地导入导出 + WebDAV 同步(坚果云)。
 */
class BackupManager(
    private val repository: SushiRepository
) {
    private val gson = Gson()
    private val csvTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 导出所有数据为 JSON 字符串
     */
    suspend fun exportToJson(): String {
        val skills = repository.getAllSkills().first()
        val professions = repository.getAllProfessions().first()
        val affixes = repository.getAllAffixes().first()
        val tasks = repository.getAllTasks().first()
        val timeRecords = repository.getAllRecords().first()
        val achievements = repository.getAllAchievements().first()
        val goals = repository.getAllGoals().first()
        val reflections = repository.getAllReflections().first()

        val backup = BackupData(
            skills = skills,
            professions = professions,
            affixes = affixes,
            tasks = tasks,
            timeRecords = timeRecords,
            achievements = achievements,
            goals = goals,
            reflections = reflections
        )

        return gson.toJson(backup)
    }

    /**
     * 从 JSON 字符串导入数据（覆盖现有数据）
     */
    suspend fun importFromJson(json: String): Result<Unit> {
        return try {
            val backup = gson.fromJson(json, BackupData::class.java)
                ?: return Result.failure(IllegalArgumentException("无效的备份数据"))

            // 清空后插入
            repository.clearAllTables()
            repository.insertSkills(backup.skills)
            if (backup.professions.isNotEmpty()) repository.insertProfessions(backup.professions)
            if (backup.affixes.isNotEmpty()) repository.insertAffixes(backup.affixes)
            backup.tasks.forEach { repository.insertTask(it) }
            backup.timeRecords.forEach { repository.insertTimeRecord(it) }
            backup.achievements.forEach { repository.insertAchievement(it) }
            backup.goals.forEach { repository.insertGoal(it) }
            backup.reflections.forEach { repository.insertReflection(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出时间记录为 CSV 格式（用于 Excel 二次分析）
     */
    suspend fun exportTimeRecordsToCsv(): String {
        val records = repository.getAllRecordsSync()
        val skills = repository.getAllSkillsSync().associateBy { it.id }
        val tasks = repository.getAllTasksSync().associateBy { it.id }

        val sb = StringBuilder()
        // UTF-8 BOM 让 Excel 正确识别中文
        sb.append("\uFEFF")
        sb.append("dateKey,start,end,rawMin,netMin,skill,task,description,interrupts,tags,isFullScreen\n")

        records.sortedBy { it.startDateTime }.forEach { r ->
            val skill = skills[r.skillId]?.name ?: ""
            val task = r.taskId?.let { tasks[it]?.name } ?: ""
            val desc = r.description.replace(",", " ").replace("\n", " ")
            val tags = r.tagsSnapshot.joinToString("|")
            sb.append("${r.attributionDateKey},")
            sb.append("${csvTimeFormat.format(Date(r.startDateTime))},")
            sb.append("${csvTimeFormat.format(Date(r.endDateTime))},")
            sb.append("${r.rawDurationMin},${r.netDurationMin},")
            sb.append("${escapeCsv(skill)},${escapeCsv(task)},")
            sb.append("${escapeCsv(desc)},${r.interruptCount},${escapeCsv(tags)},${r.isFullScreen}\n")
        }
        return sb.toString()
    }

    /**
     * 导出技能为 CSV
     */
    suspend fun exportSkillsToCsv(): String {
        val skills = repository.getAllSkillsSync()
        val sb = StringBuilder()
        sb.append("\uFEFF")
        sb.append("name,category,totalExp,level,isGraduated,graduatedAt\n")
        for (s in skills) {
            val level = s.totalExp / 120
            sb.append("${escapeCsv(s.name)},${s.category.name},${s.totalExp},${level},")
            sb.append("${s.isGraduated},${s.graduatedAt?.let { csvDateFormat.format(Date(it)) } ?: ""}\n")
        }
        return sb.toString()
    }

    /**
     * 智能合并
     */
    suspend fun mergeFromJson(json: String) {
        val cloudData = gson.fromJson(json, BackupData::class.java)
            ?: throw IllegalArgumentException("Invalid backup format")

        val localSkills = repository.getAllSkillsSync()
        val localProfessions = repository.getAllProfessionsSync()
        val localAffixes = repository.getAllAffixesSync()
        val localTasks = repository.getAllTasksSync()
        val localRecords = repository.getAllRecordsSync()
        val localAchievements = repository.getAllAchievementsSync()
        val localGoals = repository.getAllGoalsSync()
        val localReflections = repository.getAllReflectionsSync()

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
        val mergedAchievements = mergeById(
            localAchievements, cloudData.achievements, { it.id },
            { local, cloud -> if (cloud.isUnlocked && !local.isUnlocked) cloud else local }
        )
        val mergedGoals = mergeById(
            localGoals, cloudData.goals, { it.id },
            { local, cloud -> if (cloud.currentMinutes >= local.currentMinutes) cloud else local }
        )
        val mergedReflections = mergeById(
            localReflections, cloudData.reflections, { it.id },
            { local, cloud -> if (cloud.createdAt >= local.createdAt) cloud else local }
        )

        repository.insertSkills(mergedSkills)
        repository.insertProfessions(mergedProfessions)
        repository.insertAffixes(mergedAffixes)
        repository.insertAllTasks(mergedTasks)
        repository.insertAllTimeRecords(mergedRecords)
        repository.insertAllAchievements(mergedAchievements)
        mergedGoals.forEach { repository.insertGoal(it) }
        mergedReflections.forEach { repository.insertReflection(it) }
    }

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

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
    }

    fun validateJson(json: String): Boolean {
        return try {
            val backup = gson.fromJson(json, BackupData::class.java)
            backup.version > 0
        } catch (_: Exception) {
            false
        }
    }

    fun migrateBackup(data: BackupData): BackupData = when (data.version) {
        1 -> {
            // v1 → v2 升级
            data.copy(
                version = 2,
                achievements = emptyList(),
                goals = emptyList(),
                reflections = emptyList()
            )
        }
        else -> data
    }
}
