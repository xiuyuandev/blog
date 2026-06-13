package com.sushi.app.data.repository

import com.sushi.app.data.dao.*
import com.sushi.app.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SushiRepository @Inject constructor(
    private val skillDao: SkillDao,
    private val professionDao: ProfessionDao,
    private val affixDao: AffixDao,
    private val taskDao: TaskDao,
    private val timeRecordDao: TimeRecordDao
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
    suspend fun markTaskCompleted(id: String) = taskDao.markCompleted(id)

    // TimeRecords
    fun getAllRecords(): Flow<List<TimeRecord>> = timeRecordDao.getAllRecords()
    suspend fun getAllRecordsSync(): List<TimeRecord> = timeRecordDao.getAllRecordsSync()
    fun getRecordsByDate(startOfDay: Long, endOfDay: Long): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsByDate(startOfDay, endOfDay)
    fun getRecordsBySkillId(skillId: String): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsBySkillId(skillId)
    fun getRecordsByTaskId(taskId: String): Flow<List<TimeRecord>> =
        timeRecordDao.getRecordsByTaskId(taskId)
    suspend fun getTotalNetDuration(startTime: Long, endTime: Long): Int =
        timeRecordDao.getTotalNetDuration(startTime, endTime) ?: 0
    suspend fun getTotalNetDurationAll(): Int =
        timeRecordDao.getTotalNetDurationAll() ?: 0
    suspend fun insertTimeRecord(record: TimeRecord) = timeRecordDao.insert(record)
    suspend fun insertAllTimeRecords(records: List<TimeRecord>) = timeRecordDao.insertAll(records)
    suspend fun getTimeRecordCount(): Int = timeRecordDao.getCount()

    // Delete operations
    suspend fun deleteSkill(id: String) = skillDao.deleteById(id)
    suspend fun deleteTask(id: String) = taskDao.deleteById(id)
    suspend fun deleteProfession(id: String) = professionDao.deleteById(id)
    suspend fun deleteTimeRecord(id: String) = timeRecordDao.deleteById(id)
    suspend fun deleteAffix(id: String) = affixDao.deleteById(id)

    // Task reactivation and completed tasks
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    suspend fun reactivateTask(id: String) = taskDao.reactivate(id)

    // TimeRecord update
    suspend fun updateTimeRecord(record: TimeRecord) = timeRecordDao.update(record)

    // Profession name update
    suspend fun updateProfessionName(id: String, name: String) = professionDao.updateName(id, name)

    // Total pure time from TimeRecord (not from skill exp)
    suspend fun getTotalPureTimeMin(): Int = timeRecordDao.getTotalNetDurationAll() ?: 0

    // Skill name update
    suspend fun updateSkillName(id: String, name: String) {
        val skill = skillDao.getSkillById(id) ?: return
        skillDao.update(skill.copy(name = name))
    }

    // Skill category update
    suspend fun updateSkillCategory(id: String, category: SkillCategory) {
        val skill = skillDao.getSkillById(id) ?: return
        skillDao.update(skill.copy(category = category))
    }
}
