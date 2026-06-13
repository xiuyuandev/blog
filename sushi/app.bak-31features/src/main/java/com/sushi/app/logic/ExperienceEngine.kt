package com.sushi.app.logic

import com.sushi.app.data.model.*
import com.sushi.app.data.model.SkillCategory
import com.sushi.app.data.repository.SushiRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 核心经验流转引擎
 *
 * 等级算法：Level = totalExp / 120
 * 当前级进度：Progress = totalExp % 120
 *
 * 经验流转逻辑：
 * 1. 任务绑定单个技能，纯时间全额注入该技能
 * 2. 技能经验增加后，职业经验共振：遍历该技能的 linkedProfessionIds，
 *    每个关联职业的 totalExp += netDurationMin（100%等比注入）
 * 3. 属性面板计算：遍历所有技能，检查其当前等级是否满足任何 Affix 的 requiredSkillLevel
 */
@Singleton
class ExperienceEngine @Inject constructor(
    private val repository: SushiRepository
) {
    companion object {
        const val EXP_PER_LEVEL = 120
        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeKeyFormat = SimpleDateFormat("HH:00", Locale.getDefault())
    }

    fun calculateLevel(totalExp: Int): Int = totalExp / EXP_PER_LEVEL

    private val categoryPrimaryAttribute = mapOf(
        SkillCategory.COGNITION to AttributeType.INTELLECT,
        SkillCategory.CREATION to AttributeType.CREATION,
        SkillCategory.FUNCTION to AttributeType.PHYSIQUE,
        SkillCategory.STRATEGY to AttributeType.INSIGHT
    )

    fun calculateProgress(totalExp: Int): Int = totalExp % EXP_PER_LEVEL

    fun getVisualTier(level: Int): VisualTier {
        return when {
            level < 10 -> VisualTier.RAW_STONE
            level < 30 -> VisualTier.BRONZE
            level < 100 -> VisualTier.RED_GOLD
            else -> VisualTier.OBSIDIAN
        }
    }

    /**
     * 通过任务结算纯时间
     */
    suspend fun settleTime(
        taskId: String,
        rawDurationMin: Int,
        netDurationMin: Int,
        startDateTime: Long,
        endDateTime: Long,
        description: String,
        interruptCount: Int = 0,
        lastPauseReason: String? = null,
        tagsSnapshot: List<String> = emptyList(),
        isFullScreen: Boolean = false
    ): SettlementResult {
        val task = repository.getTaskById(taskId)
            ?: return SettlementResult.Error("任务不存在")

        val skillId = task.linkedSkillId
        val skill = repository.getSkillById(skillId)
            ?: return SettlementResult.Error("关联技能不存在")

        return injectExpToSkill(
            skillId = skillId,
            netDurationMin = netDurationMin,
            taskId = taskId,
            rawDurationMin = rawDurationMin,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            description = description,
            isManualEntry = false,
            interruptCount = interruptCount,
            lastPauseReason = lastPauseReason,
            tagsSnapshot = tagsSnapshot,
            isFullScreen = isFullScreen
        )
    }

    /**
     * 手动向技能注入时间
     */
    suspend fun manualInject(
        skillId: String,
        netDurationMin: Int,
        startDateTime: Long,
        endDateTime: Long,
        description: String
    ): SettlementResult {
        val skill = repository.getSkillById(skillId)
            ?: return SettlementResult.Error("技能不存在")

        return injectExpToSkill(
            skillId = skillId,
            netDurationMin = netDurationMin,
            taskId = null,
            rawDurationMin = netDurationMin,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            description = description,
            isManualEntry = true
        )
    }

    /**
     * 核心经验注入逻辑
     */
    private suspend fun injectExpToSkill(
        skillId: String,
        netDurationMin: Int,
        taskId: String?,
        rawDurationMin: Int,
        startDateTime: Long,
        endDateTime: Long,
        description: String,
        isManualEntry: Boolean,
        interruptCount: Int = 0,
        lastPauseReason: String? = null,
        tagsSnapshot: List<String> = emptyList(),
        isFullScreen: Boolean = false
    ): SettlementResult {
        val skill = repository.getSkillById(skillId) ?: return SettlementResult.Error("技能不存在")
        val oldLevel = calculateLevel(skill.totalExp)
        val newExp = skill.totalExp + netDurationMin
        val newLevel = calculateLevel(newExp)

        repository.updateSkillExp(skillId, newExp)

        val levelUpEvents = mutableListOf<LevelUpEvent>()
        val unlockedAffixes = mutableListOf<Affix>()

        if (newLevel > oldLevel) {
            levelUpEvents.add(
                LevelUpEvent(
                    skillId = skillId,
                    skillName = skill.name,
                    oldLevel = oldLevel,
                    newLevel = newLevel
                )
            )
            val affixes = repository.getAffixesBySkillIds(listOf(skillId))
            for (affix in affixes) {
                if (affix.requiredSkillLevel in (oldLevel + 1)..newLevel) {
                    unlockedAffixes.add(affix)
                }
            }
        }

        // 职业经验共振
        for (professionId in skill.linkedProfessionIds) {
            val profession = repository.getProfessionById(professionId) ?: continue
            val newProfessionExp = profession.totalExp + netDurationMin
            repository.updateProfessionExp(professionId, newProfessionExp)
        }

        // 插入时间记录（含跨日归属 - 按开始时间归入哪一天）
        val attributionKey = dateKeyFormat.format(Date(startDateTime))
        val record = TimeRecord(
            id = java.util.UUID.randomUUID().toString(),
            skillId = skillId,
            taskId = taskId,
            rawDurationMin = rawDurationMin,
            netDurationMin = netDurationMin,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            description = description,
            isManualEntry = isManualEntry,
            timestamp = System.currentTimeMillis(),
            attributionDateKey = attributionKey,
            interruptCount = interruptCount,
            lastPauseReason = lastPauseReason,
            tagsSnapshot = tagsSnapshot,
            isFullScreen = isFullScreen
        )
        repository.insertTimeRecord(record)

        val attributes = recalculateAttributes()

        // 更新目标进度
        updateGoalProgress(record)

        // 评估成就
        val unlockedAchievements = checkAchievements()

        return SettlementResult.Success(
            timeRecord = record,
            levelUpEvents = levelUpEvents,
            unlockedAffixes = unlockedAffixes,
            updatedAttributes = attributes,
            unlockedAchievements = unlockedAchievements
        )
    }

    suspend fun recalculateAttributes(): Map<AttributeType, Int> {
        val attributes = mutableMapOf<AttributeType, Int>(
            AttributeType.PHYSIQUE to 0,
            AttributeType.INTELLECT to 0,
            AttributeType.CREATION to 0,
            AttributeType.INSIGHT to 0,
            AttributeType.DOMINION to 0
        )

        val allSkills = repository.getAllSkills().first()
        val allAffixList = repository.getAllAffixes().first()

        for (skill in allSkills) {
            val level = calculateLevel(skill.totalExp)
            val primaryAttr = categoryPrimaryAttribute[skill.category] ?: continue
            attributes[primaryAttr] = (attributes[primaryAttr] ?: 0) + level
        }

        for (skill in allSkills) {
            val level = calculateLevel(skill.totalExp)
            for (affix in allAffixList) {
                if (affix.requiredSkillId == skill.id && level >= affix.requiredSkillLevel) {
                    for ((attrType, bonus) in affix.bonusAttributes) {
                        attributes[attrType] = (attributes[attrType] ?: 0) + bonus
                    }
                }
            }
        }

        return attributes
    }

    suspend fun recalculateProfessionExp(professionId: String) {
        val profession = repository.getProfessionById(professionId) ?: return
        val allSkills = repository.getAllSkills().first()
        val linkedSkillExp = allSkills
            .filter { professionId in it.linkedProfessionIds }
            .sumOf { it.totalExp }
        repository.updateProfessionExp(professionId, linkedSkillExp)
    }

    // ====== 新增功能 ======

    /**
     * 计算当前连续记录天数（Streak）
     * 倒推：今天算起，连续 1+N 天有任意纯时间记录
     */
    suspend fun calculateStreak(): StreakInfo {
        val dailyTotals = repository.getDailyTotalsRaw()
        val activeDates = dailyTotals
            .filter { it.totalMin > 0 }
            .map { it.attributionDateKey }
            .toSet()

        if (activeDates.isEmpty()) {
            return StreakInfo(currentStreak = 0, longestStreak = 0, totalActiveDays = 0)
        }

        val today = dateKeyFormat.format(Date())
        val yesterday = dateKeyFormat.format(Date(System.currentTimeMillis() - 24 * 3600 * 1000L))

        // 当前连续：今天或昨天为锚点向前数
        val anchor = when {
            today in activeDates -> today
            yesterday in activeDates -> yesterday
            else -> null
        }

        var currentStreak = 0
        if (anchor != null) {
            val cal = Calendar.getInstance().apply {
                time = dateKeyFormat.parse(anchor) ?: Date()
            }
            while (dateKeyFormat.format(cal.time) in activeDates) {
                currentStreak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            }
        }

        // 最长连续：扫描所有日期找到最长连续段
        val sortedDates = activeDates.sorted()
        var longest = 0
        var runLen = 0
        var prevCal: Calendar? = null
        for (key in sortedDates) {
            val cal = Calendar.getInstance().apply {
                time = dateKeyFormat.parse(key) ?: Date()
            }
            if (prevCal == null) {
                runLen = 1
            } else {
                val diff = ((cal.timeInMillis - prevCal.timeInMillis) / (24 * 3600 * 1000L)).toInt()
                if (diff == 1) {
                    runLen++
                } else if (diff > 1) {
                    runLen = 1
                }
                // diff == 0 不可能因为是 Set
            }
            if (runLen > longest) longest = runLen
            prevCal = cal
        }

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longest,
            totalActiveDays = activeDates.size
        )
    }

    /**
     * 生成 N 天热力数据 (dateKey -> totalMin)
     */
    suspend fun generateHeatmap(days: Int = 365): Map<String, Int> {
        val dailyTotals = repository.getDailyTotalsRaw().associate { it.attributionDateKey to it.totalMin }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1))
        val result = LinkedHashMap<String, Int>()
        repeat(days) {
            val key = dateKeyFormat.format(cal.time)
            result[key] = dailyTotals[key] ?: 0
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }

    /**
     * 技能占比 - (skillId -> totalMin)
     */
    suspend fun skillDistribution(): Map<String, Int> {
        val allRecords = repository.getAllRecordsSync()
        val skills = repository.getAllSkillsSync().associateBy { it.id }
        val result = mutableMapOf<String, Int>()
        for (record in allRecords) {
            if (record.isManualEntry) continue
            val key = record.skillId
            result[key] = (result[key] ?: 0) + record.netDurationMin
        }
        return result.mapKeys { skills[it.key]?.name ?: "未知" }
    }

    /**
     * 专注时段分析 - 24 小时分布（key 为 "HH:00"）
     */
    suspend fun activePeriodAnalysis(): Map<String, Int> {
        val allRecords = repository.getAllRecordsSync()
        val cal = Calendar.getInstance()
        val result = mutableMapOf<String, Int>()
        for (hour in 0..23) {
            result["%02d:00".format(hour)] = 0
        }
        for (record in allRecords) {
            cal.timeInMillis = record.startDateTime
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val key = "%02d:00".format(hour)
            result[key] = (result[key] ?: 0) + record.netDurationMin
        }
        return result
    }

    /**
     * 周报数据生成
     */
    suspend fun generateWeeklyReport(referenceTime: Long): WeeklyReport {
        val cal = Calendar.getInstance().apply { timeInMillis = referenceTime }
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = cal.timeInMillis

        val totalMin = repository.getTotalNetDuration(weekStart, weekEnd)
        val records = repository.getRecordsSince(weekStart)

        // 上周对比
        val lastWeekStart = weekStart - 7 * 24 * 3600 * 1000L
        val lastWeekMin = repository.getTotalNetDuration(lastWeekStart, weekStart)

        // 各技能占比
        val skillMinMap = mutableMapOf<String, Int>()
        for (record in records) {
            if (record.isManualEntry) continue
            skillMinMap[record.skillId] = (skillMinMap[record.skillId] ?: 0) + record.netDurationMin
        }
        val skills = repository.getAllSkillsSync().associateBy { it.id }
        val topSkillId = skillMinMap.maxByOrNull { it.value }?.key
        val topSkillName = topSkillId?.let { skills[it]?.name } ?: "无"

        // 中断统计
        val totalInterrupts = records.sumOf { it.interruptCount }
        val avgInterrupts = if (records.isNotEmpty()) totalInterrupts.toFloat() / records.size else 0f

        return WeeklyReport(
            weekStart = weekStart,
            weekEnd = weekEnd,
            totalMin = totalMin,
            recordCount = records.size,
            topSkillName = topSkillName,
            topSkillMin = skillMinMap[topSkillId] ?: 0,
            lastWeekMin = lastWeekMin,
            avgInterruptsPerSession = avgInterrupts,
            skillDistribution = skillMinMap.mapKeys { skills[it.key]?.name ?: "未知" }
        )
    }

    /**
     * 更新所有活跃目标的进度
     */
    private suspend fun updateGoalProgress(record: TimeRecord) {
        val activeGoals = repository.getAllGoalsSync().filter { it.isActive }
        for (goal in activeGoals) {
            // 目标时间窗内才计算
            if (record.startDateTime < goal.startTime || record.startDateTime >= goal.endTime) continue
            // 限定技能
            if (goal.scopeSkillId != null && record.skillId != goal.scopeSkillId) continue
            // 手动注入是否计入（默认计入）
            val delta = record.netDurationMin
            val newCurrent = goal.currentMinutes + delta
            repository.updateGoalProgress(goal.id, newCurrent)
        }
    }

    /**
     * 评估并解锁成就
     */
    private suspend fun checkAchievements(): List<Achievement> {
        val allAchievements = repository.getAllAchievementsSync()
        val records = repository.getAllRecordsSync()
        val skills = repository.getAllSkillsSync()
        val totalMin = records.sumOf { it.netDurationMin }
        val recordCount = records.size
        val skillCount = skills.size
        val maxLevel = skills.maxOfOrNull { calculateLevel(it.totalExp) } ?: 0
        val streak = calculateStreak()
        val allReflections = repository.getAllReflectionsSync()
        val reflectionCount = allReflections.size

        val unlocked = mutableListOf<Achievement>()
        for (ach in allAchievements) {
            if (ach.isUnlocked) continue
            val progress = when (ach.conditionType) {
                "STREAK" -> streak.currentStreak
                "TOTAL_TIME" -> totalMin
                "SKILL_COUNT" -> skillCount
                "SKILL_LEVEL" -> maxLevel
                "DAILY_REFLECTION" -> reflectionCount
                "CONSISTENT_WEEK" -> streak.longestStreak
                "RECORD_COUNT" -> recordCount
                else -> 0
            }
            if (progress != ach.currentProgress) {
                repository.updateAchievementProgress(ach.id, progress)
            }
            if (progress >= ach.threshold) {
                repository.unlockAchievement(ach.id)
                unlocked.add(ach.copy(isUnlocked = true, currentProgress = progress, unlockedAt = System.currentTimeMillis()))
            }
        }
        return unlocked
    }

    /**
     * 初始化默认成就（首次启动时）
     */
    suspend fun seedDefaultAchievements() {
        if (repository.getAllAchievementsSync().isNotEmpty()) return
        val defaults = listOf(
            Achievement("a_streak_3", "三日不辍", "连续 3 天记录纯时间", "STREAK", "STREAK", 3, tierLabel = "铜"),
            Achievement("a_streak_7", "七日成章", "连续 7 天记录纯时间", "STREAK", "STREAK", 7, tierLabel = "银"),
            Achievement("a_streak_30", "月有所成", "连续 30 天记录纯时间", "STREAK", "STREAK", 30, tierLabel = "金"),
            Achievement("a_time_60", "初窥门径", "累计纯时间 1 小时", "TIME", "TOTAL_TIME", 60, tierLabel = "铜"),
            Achievement("a_time_600", "积少成多", "累计纯时间 10 小时", "TIME", "TOTAL_TIME", 600, tierLabel = "银"),
            Achievement("a_time_3000", "持之以恒", "累计纯时间 50 小时", "TIME", "TOTAL_TIME", 3000, tierLabel = "金"),
            Achievement("a_skill_3", "三足鼎立", "拥有 3 个技能", "SKILL", "SKILL_COUNT", 3, tierLabel = "铜"),
            Achievement("a_skill_8", "诸艺精通", "拥有 8 个技能", "SKILL", "SKILL_COUNT", 8, tierLabel = "银"),
            Achievement("a_level_30", "赤金之身", "任意技能达到 30 级", "LEVEL", "SKILL_LEVEL", 30, tierLabel = "银"),
            Achievement("a_level_100", "黑曜之境", "任意技能达到 100 级", "LEVEL", "SKILL_LEVEL", 100, tierLabel = "金"),
            Achievement("a_reflect_7", "反思者", "完成 7 次每日反思", "REFLECT", "DAILY_REFLECTION", 7, tierLabel = "铜"),
            Achievement("a_reflect_30", "日省吾身", "完成 30 次每日反思", "REFLECT", "DAILY_REFLECTION", 30, tierLabel = "银"),
            Achievement("a_consistent_7", "周全", "曾经连续 7 天记录", "STREAK", "CONSISTENT_WEEK", 7, tierLabel = "铜"),
            Achievement("a_records_50", "勤勉", "完成 50 次专注记录", "COUNT", "RECORD_COUNT", 50, tierLabel = "银")
        )
        repository.insertAllAchievements(defaults)
    }

    /**
     * 预置帮助中心条目
     */
    suspend fun seedHelpEntries() {
        if (repository.getAllHelpEntriesSync().isNotEmpty()) return
        val entries = listOf(
            HelpEntry("h_pure_time", "什么是纯时间？", "柳比歇夫将时间分为『纯时间』与『杂质时间』。纯时间指真正专注在所选事务上的时间，杂质时间指被打断、切换、走神等。结算时诚实地扣除杂质时间，得到的才是纯时间。", "概念", 1),
            HelpEntry("h_why_settle", "为什么要结算？", "结算让你对自己的时间有真实的认知。不是所有流逝的时间都算纯时间——你专注 1 小时，期间被切走 3 次，可能只有 40 分钟是纯时间。这种诚实是关键。", "概念", 2),
            HelpEntry("h_skill_category", "技能分类的意义", "4 个分类对应 5 个属性的主属性：认知→智识、造物→造物、功能→体魄、策略→洞察、统御→统御。技能等级自动贡献给分类主属性。", "概念", 3),
            HelpEntry("h_tier", "四级视觉材质", "原石（1-9）→ 青铜（10-29）→ 赤金（30-99）→ 黑曜石（100+）。等级越高视觉冲击越强。", "概念", 4),
            HelpEntry("h_how_to_start", "如何开始一次专注？", "点击底部「专注」Tab → 选择任务或技能 → 点击「开始」即进入专注。专注中可以暂停，停止后弹出结算对话框。", "入门", 1),
            HelpEntry("h_create_skill", "如何创建技能？", "「技能」Tab → 点击右下角 + → 输入名称并选择分类即可。每个技能对应一个主属性贡献点。", "入门", 2),
            HelpEntry("h_create_task", "如何创建任务？", "「专注」Tab → 点击「新任务」→ 输入名称并绑定技能。任务完成后可重新激活。", "入门", 3),
            HelpEntry("h_manual_inject", "如何补录历史时间？", "进入技能详情 → 点击「手动注入」→ 选择时长与时段。这用于补录之前忘记结算的纯时间。", "功能", 1),
            HelpEntry("h_profession", "职业是什么？", "职业是若干技能的合集。职业经验等于其关联技能经验之和。绑定到职业的技能升级也会为该职业贡献经验。", "功能", 2),
            HelpEntry("h_graduation", "技能毕业是什么？", "技能达到 LV 100 后可主动「毕业」。毕业后的技能会进入「已毕业」分组，留下寄语。这是一个里程碑仪式。", "功能", 3),
            HelpEntry("h_streak", "Streak 是什么？", "连续记录天数。今天或昨天有记录时开始计算，每断一天重新归零。Streak 越长越难维持，是习惯养成的核心指标。", "功能", 4),
            HelpEntry("h_sync_webdav", "WebDAV 同步说明", "在「同步与备份」中选择 WebDAV，填入坚果云/自建服务器的地址、账号、第三方应用密码，即可定时同步。", "同步", 1),
            HelpEntry("h_sync_s3", "S3 同步说明", "支持 S3 兼容协议（AWS、七牛、阿里 OSS、MinIO）。填入 Endpoint、Region、Bucket、Access Key 即可。", "同步", 2),
            HelpEntry("h_sync_conflict", "同步冲突如何处理？", "同时多端编辑时会产生冲突条目，在「同步与备份」中查看。可选择本地覆盖、远端覆盖或合并。", "同步", 3),
            HelpEntry("h_import_json", "如何导入旧数据？", "「同步与备份」→「本地备份」→「导入 JSON」选择之前的备份文件。导入前可预览，导入会清空当前数据。", "同步", 4),
            HelpEntry("h_crash", "应用闪退怎么办？", "进入「设置」→「数据恢复」从最近的自动备份恢复。也可在「同步与备份」中先拉取云端备份再覆盖。", "故障", 1),
            HelpEntry("h_lost_data", "我删错了数据", "所有删除操作都有 4 秒的撤销提示。超过 4 秒后请从云端/JSON 备份恢复。", "故障", 2)
        )
        repository.insertHelpEntries(entries)
    }
}

enum class VisualTier(
    val label: String,
    val colorHex: String,
    val glowHex: String?
) {
    RAW_STONE("原石", "#757575", null),
    BRONZE("青铜", "#B87333", null),
    RED_GOLD("赤金", "#FFBF00", "#FFBF0040"),
    OBSIDIAN("黑曜石", "#1A1A1A", "#00FF7F")
}

sealed class SettlementResult {
    data class Success(
        val timeRecord: TimeRecord,
        val levelUpEvents: List<LevelUpEvent>,
        val unlockedAffixes: List<Affix>,
        val updatedAttributes: Map<AttributeType, Int>,
        val unlockedAchievements: List<Achievement> = emptyList()
    ) : SettlementResult()

    data class Error(val message: String) : SettlementResult()
}

data class LevelUpEvent(
    val skillId: String,
    val skillName: String,
    val oldLevel: Int,
    val newLevel: Int
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalActiveDays: Int
)

data class WeeklyReport(
    val weekStart: Long,
    val weekEnd: Long,
    val totalMin: Int,
    val recordCount: Int,
    val topSkillName: String,
    val topSkillMin: Int,
    val lastWeekMin: Int,
    val avgInterruptsPerSession: Float,
    val skillDistribution: Map<String, Int>
)
