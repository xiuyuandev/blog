package com.sushi.app.logic

import com.sushi.app.data.model.*
import com.sushi.app.data.model.SkillCategory
import com.sushi.app.data.repository.SushiRepository
import kotlinx.coroutines.flow.first
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
        description: String
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
            isManualEntry = false
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
        isManualEntry: Boolean
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

        // 插入时间记录
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
            timestamp = System.currentTimeMillis()
        )
        repository.insertTimeRecord(record)

        val attributes = recalculateAttributes()

        return SettlementResult.Success(
            timeRecord = record,
            levelUpEvents = levelUpEvents,
            unlockedAffixes = unlockedAffixes,
            updatedAttributes = attributes
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

        // Base attributes: each skill contributes its level to its category's primary attribute
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

    /**
     * Recalculate profession totalExp from its linked skills' totalExp sum.
     * Call this when skills are added/removed from a profession.
     */
    suspend fun recalculateProfessionExp(professionId: String) {
        val profession = repository.getProfessionById(professionId) ?: return
        val allSkills = repository.getAllSkills().first()
        val linkedSkillExp = allSkills
            .filter { professionId in it.linkedProfessionIds }
            .sumOf { it.totalExp }
        repository.updateProfessionExp(professionId, linkedSkillExp)
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
        val updatedAttributes: Map<AttributeType, Int>
    ) : SettlementResult()

    data class Error(val message: String) : SettlementResult()
}

data class LevelUpEvent(
    val skillId: String,
    val skillName: String,
    val oldLevel: Int,
    val newLevel: Int
)
