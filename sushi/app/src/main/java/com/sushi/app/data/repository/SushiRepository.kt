package com.sushi.app.data.repository

import com.sushi.app.data.dao.*
import com.sushi.app.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 仓库层:聚合 11 个 DAO。
 *
 * 由 [com.sushi.app.SushiContainer] 持有单例,不再依赖 Hilt。
 */
class SushiRepository(
    private val skillDao: SkillDao,
    private val professionDao: ProfessionDao,
    private val affixDao: AffixDao,
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao,
    private val achievementDao: AchievementDao,
    private val goalDao: GoalDao,
    private val dailyReflectionDao: DailyReflectionDao,
    private val pauseLogDao: PauseLogDao,
    private val syncConflictDao: SyncConflictDao,
    private val helpEntryDao: HelpEntryDao
) {
    // Skills
    fun getAllSkills(): Flow<List<Skill>> = skillDao.getAllSkills()
    suspend fun getAllSkillsSync(): List<Skill> = skillDao.getAllSkillsSync()
    fun getSkillsByCategory(category: SkillCategory): Flow<List<Skill>> =
        skillDao.getSkillsByCategory(category.name)
    suspend fun getSkillById(id: String): Skill? = skillDao.getSkillById(id)
    suspend fun insertSkill(skill: Skill) = skillDao.insert(skill)
    suspend fun insertSkills(skills: List<Skill>) = skillDao.insertAll(skills)
    suspend fun updateSkill(skill: Skill) = skillDao.update(skill)
    suspend fun updateSkillExp(id: String, totalExp: Int) = skillDao.updateExp(id, totalExp)
    suspend fun updateLinkedProfessions(id: String, professionIds: List<String>) =
        skillDao.updateLinkedProfessions(id, professionIds)
    suspend fun updatePrerequisites(id: String, prereqIds: List<String>) =
        skillDao.updatePrerequisites(id, prereqIds)
    suspend fun graduateSkill(id: String, message: String) =
        skillDao.graduate(id, System.currentTimeMillis(), message)
    fun getGraduatedSkills(): Flow<List<Skill>> = skillDao.getGraduatedSkills()

    // Professions
    fun getAllProfessions(): Flow<List<Profession>> = professionDao.getAllProfessions()
    suspend fun getAllProfessionsSync(): List<Profession> = professionDao.getAllProfessionsSync()
    suspend fun getProfessionById(id: String): Profession? = professionDao.getProfessionById(id)
    suspend fun insertProfession(profession: Profession) = professionDao.insert(profession)
    suspend fun insertProfessions(professions: List<Profession>) = professionDao.insertAll(professions)
    suspend fun updateProfessionExp(id: String, totalExp: Int) = professionDao.updateExp(id, totalExp)

    // Affixes
    fun getAllAffixes(): Flow<List<Affix>> = affixDao.getAllAffixes()
    suspend fun getAllAffixesSync(): List<Affix> = affixDao.getAllAffixesSync()
    fun getAffixesBySkillId(skillId: String): Flow<List<Affix>> = affixDao.getAffixesBySkillId(skillId)
    suspend fun getAffixesBySkillIds(skillIds: List<String>): List<Affix> =
        if (skillIds.isEmpty()) emptyList() else affixDao.getAffixesBySkillIds(skillIds)
    suspend fun insertAffixes(affixes: List<Affix>) = affixDao.insertAll(affixes)

    // Tasks
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    suspend fun getAllTasksSync(): List<Task> = taskDao.getAllTasksSync()
    suspend fun getTaskById(id: String): Task? = taskDao.getTaskById(id)
    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun insertAllTasks(tasks: List<Task>) = taskDao.insertAll(tasks)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun markTaskCompleted(id: String) = taskDao.markCompleted(id)
    fun getTemplateTasks(): Flow<List<Task>> = taskDao.getTemplateTasks()
    suspend fun getTemplateTasksSync(): List<Task> = taskDao.getTemplateTasksSync()
    fun getRecurringTasks(): Flow<List<Task>> = taskDao.getRecurringTasks()
    fun getTasksByProfession(professionId: String): Flow<List<Task>> = taskDao.getByProfession(professionId)

    // TimeRecords
    fun getAllRecords(): Flow<List<TimeRecord>> = timeRecordDao.getAllRecords()
    suspend fun getAllRecordsSync(): List<TimeRecord> = timeRecordDao.getAllRecordsSync()
    fun getRecordsByDate(startOfDay: Long, endOfDay: Long): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsByDate(startOfDay, endOfDay)
    fun getRecordsByDateKey(dateKey: String): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsByDateKey(dateKey)
    fun getRecordsBySkillId(skillId: String): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsBySkillId(skillId)
    fun getRecordsByTaskId(taskId: String): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsByTaskId(taskId)
    suspend fun getTotalNetDuration(startTime: Long, endTime: Long): Int =
        timeRecordDao.getTotalNetDuration(startTime, endTime) ?: 0
    suspend fun getTotalNetDurationByDateKey(dateKey: String): Int =
        timeRecordDao.getTotalNetDurationByDateKey(dateKey) ?: 0
    suspend fun getTotalNetDurationAll(): Int =
        timeRecordDao.getTotalNetDurationAll() ?: 0
    suspend fun getDailyTotalsRaw(): List<DailyTotalRaw> = timeRecordDao.getDailyTotalsRaw()
    suspend fun getRecordsSince(startTime: Long): List<TimeRecord> = timeRecordDao.getRecordsSince(startTime)
    suspend fun insertTimeRecord(record: TimeRecord) = timeRecordDao.insert(record)
    suspend fun insertAllTimeRecords(records: List<TimeRecord>) = timeRecordDao.insertAll(records)
    suspend fun updateTimeRecord(record: TimeRecord) = timeRecordDao.update(record)
    suspend fun getTimeRecordCount(): Int = timeRecordDao.getCount()

    // Achievements
    fun getAllAchievements(): Flow<List<Achievement>> = achievementDao.getAllAchievements()
    suspend fun getAllAchievementsSync(): List<Achievement> = achievementDao.getAllAchievementsSync()
    fun getUnlockedAchievements(): Flow<List<Achievement>> = achievementDao.getUnlockedAchievements()
    suspend fun insertAchievement(achievement: Achievement) = achievementDao.insert(achievement)
    suspend fun insertAllAchievements(achievements: List<Achievement>) = achievementDao.insertAll(achievements)
    suspend fun updateAchievement(achievement: Achievement) = achievementDao.update(achievement)
    suspend fun updateAchievementProgress(id: String, progress: Int) = achievementDao.updateProgress(id, progress)
    suspend fun unlockAchievement(id: String) = achievementDao.markUnlocked(id, System.currentTimeMillis())
    suspend fun getUnlockedAchievementCount(): Int = achievementDao.getUnlockedCount()

    // Goals
    fun getActiveGoals(): Flow<List<Goal>> = goalDao.getActiveGoals()
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()
    suspend fun getAllGoalsSync(): List<Goal> = goalDao.getAllGoalsSync()
    suspend fun insertGoal(goal: Goal) = goalDao.insert(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.update(goal)
    suspend fun updateGoalProgress(id: String, currentMinutes: Int) = goalDao.updateProgress(id, currentMinutes)
    suspend fun deactivateGoal(id: String) = goalDao.deactivate(id)
    suspend fun deleteGoal(id: String) = goalDao.deleteById(id)

    // Daily Reflections
    fun getAllReflections(): Flow<List<DailyReflection>> = dailyReflectionDao.getAllReflections()
    suspend fun getReflectionByDate(dateKey: String): DailyReflection? =
        dailyReflectionDao.getReflectionByDate(dateKey)
    suspend fun insertReflection(reflection: DailyReflection) = dailyReflectionDao.insert(reflection)
    suspend fun updateReflection(reflection: DailyReflection) = dailyReflectionDao.update(reflection)
    suspend fun deleteReflection(id: String) = dailyReflectionDao.deleteById(id)

    // Pause Logs
    fun getAllPauseLogs(): Flow<List<PauseLog>> = pauseLogDao.getAllPauseLogs()
    suspend fun getAllPauseLogsSync(): List<PauseLog> = pauseLogDao.getAllPauseLogsSync()
    fun getPauseLogsByRecord(recordId: String): Flow<List<PauseLog>> = pauseLogDao.getPauseLogsByRecord(recordId)
    suspend fun insertPauseLog(log: PauseLog) = pauseLogDao.insert(log)
    suspend fun insertAllPauseLogs(logs: List<PauseLog>) = pauseLogDao.insertAll(logs)
    suspend fun deletePauseLogsByRecord(recordId: String) = pauseLogDao.deleteByRecordId(recordId)

    // Sync Conflicts
    fun getPendingConflicts(): Flow<List<SyncConflict>> = syncConflictDao.getPendingConflicts()
    suspend fun insertConflict(conflict: SyncConflict) = syncConflictDao.insert(conflict)
    suspend fun resolveConflict(id: String, resolution: String) = syncConflictDao.updateResolution(id, resolution)
    suspend fun deleteConflict(id: String) = syncConflictDao.deleteById(id)

    // Help Entries
    fun getAllHelpEntries(): Flow<List<HelpEntry>> = helpEntryDao.getAllEntries()
    suspend fun getAllHelpEntriesSync(): List<HelpEntry> = helpEntryDao.getAllSync()
    suspend fun insertHelpEntries(entries: List<HelpEntry>) = helpEntryDao.insertAll(entries)

    // 清空所有表（用于"覆盖"导入）
    suspend fun clearAllTables() {
        timeRecordDao.deleteAll()
        affixDao.deleteAll()
        taskDao.deleteAll()
        skillDao.deleteAll()
        professionDao.deleteAll()
        achievementDao.deleteAll()
        goalDao.deleteAll()
        dailyReflectionDao.deleteAll()
        pauseLogDao.deleteAll()
        syncConflictDao.deleteAll()
        helpEntryDao.deleteAll()
    }

    // Delete operations
    suspend fun deleteSkill(id: String) {
        timeRecordDao.deleteBySkillId(id)
        affixDao.deleteBySkillId(id)
        taskDao.deleteActiveBySkillId(id)
        skillDao.deleteById(id)
    }
    suspend fun deleteTask(id: String) = taskDao.deleteById(id)

    suspend fun deleteProfession(id: String) {
        val allSkills = skillDao.getAllSkillsSync()
        for (skill in allSkills) {
            if (id in skill.linkedProfessionIds) {
                val updated = skill.linkedProfessionIds.filter { it != id }
                skillDao.updateLinkedProfessions(skill.id, updated)
            }
        }
        professionDao.deleteById(id)
    }
    suspend fun deleteTimeRecord(id: String) {
        pauseLogDao.deleteByRecordId(id)
        timeRecordDao.deleteById(id)
    }
    suspend fun deleteAffix(id: String) = affixDao.deleteById(id)

    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    suspend fun reactivateTask(id: String) = taskDao.reactivate(id)

    suspend fun updateProfessionName(id: String, name: String) = professionDao.updateName(id, name)

    suspend fun getTotalPureTimeMin(): Int = timeRecordDao.getTotalNetDurationAll() ?: 0

    suspend fun updateSkillName(id: String, name: String) {
        val skill = skillDao.getSkillById(id) ?: return
        skillDao.update(skill.copy(name = name))
    }

    suspend fun updateSkillCategory(id: String, category: SkillCategory) {
        val skill = skillDao.getSkillById(id) ?: return
        skillDao.update(skill.copy(category = category))
    }
}
