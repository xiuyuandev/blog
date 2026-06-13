package com.sushi.app.logic

import com.sushi.app.data.model.*
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
 * 经验流转逻辑（4.3节）：
 * 1. 获取任务绑定的技能（1~2个）
 * 2. 若绑定1个技能：该技能 totalExp += netDurationMin
 *    若绑定2个技能：两个技能各自 totalExp += (netDurationMin / 2)（向下取整）
 * 3. 职业经验共振：技能经验增加后，遍历该技能的 linkedProfessionIds，
 *    每个关联职业的 totalExp += netDurationMin（100%等比注入，不平分）
 * 4. 属性面板计算：遍历所有技能，检查其当前等级是否满足任何 Affix 的 requiredSkillLevel。
 *    若满足，将该 Affix 的 bonusAttributes 累加到用户总面板属性中。
 */
@Singleton
class ExperienceEngine @Inject constructor(
    private val repository: SushiRepository
) {
    companion object {
        const val EXP_PER_LEVEL = 120
    }

    /**
     * 计算等级
     */
    fun calculateLevel(totalExp: Int): Int = totalExp / EXP_PER_LEVEL

    /**
     * 计算当前级进度
     */
    fun calculateProgress(totalExp: Int): Int = totalExp % EXP_PER_LEVEL

    /**
     * 视觉等级区间映射
     */
    fun getVisualTier(level: Int): VisualTier {
        return when {
            level < 10 -> VisualTier.RAW_STONE
            level < 30 -> VisualTier.BRONZE
            level < 100 -> VisualTier.RED_GOLD
            else -> VisualTier.OBSIDIAN
        }
    }

    /**
     * 核心经验流转：结算一次纯时间记录
     *
     * @param taskId 任务ID
     * @param rawDurationMin 原始时长（分钟）
     * @param netDurationMin 纯时间（分钟）
     * @return SettlementResult 结算结果，包含升级信息、解锁词条等
     */
    suspend fun settleTime(taskId: String, rawDurationMin: Int, netDurationMin: Int): SettlementResult {
        val task = repository.getTaskById(taskId) ?: return SettlementResult.Error("任务不存在")

        // 1. 获取任务绑定的技能
        val linkedSkillIds = task.linkedSkillIds
        if (linkedSkillIds.isEmpty()) {
            return SettlementResult.Error("任务未绑定任何技能")
        }

        val skillExpGain = if (linkedSkillIds.size == 1) {
            netDurationMin
        } else {
            netDurationMin / 2 // 一心二用，纯度下降
        }

        val levelUpEvents = mutableListOf<LevelUpEvent>()
        val unlockedAffixes = mutableListOf<Affix>()

        // 2. 技能经验分配
        for (skillId in linkedSkillIds) {
            val skill = repository.getSkillById(skillId) ?: continue
            val oldLevel = calculateLevel(skill.totalExp)
            val newExp = skill.totalExp + skillExpGain
            val newLevel = calculateLevel(newExp)

            repository.updateSkillExp(skillId, newExp)

            // 检测升级
            if (newLevel > oldLevel) {
                levelUpEvents.add(
                    LevelUpEvent(
                        skillId = skillId,
                        skillName = skill.name,
                        oldLevel = oldLevel,
                        newLevel = newLevel
                    )
                )

                // 检查是否解锁新词条
                val affixes = repository.getAffixesBySkillIds(listOf(skillId))
                for (affix in affixes) {
                    if (affix.requiredSkillLevel in (oldLevel + 1)..newLevel) {
                        unlockedAffixes.add(affix)
                    }
                }
            }

            // 3. 职业经验共振
            for (professionId in skill.linkedProfessionIds) {
                val profession = repository.getProfessionById(professionId) ?: continue
                val newProfessionExp = profession.totalExp + netDurationMin // 100%等比注入
                repository.updateProfessionExp(professionId, newProfessionExp)
            }
        }

        // 4. 插入时间记录
        val record = TimeRecord(
            id = java.util.UUID.randomUUID().toString(),
            taskId = taskId,
            rawDurationMin = rawDurationMin,
            netDurationMin = netDurationMin,
            timestamp = System.currentTimeMillis()
        )
        repository.insertTimeRecord(record)

        // 5. 计算更新后的属性面板
        val attributes = recalculateAttributes()

        return SettlementResult.Success(
            timeRecord = record,
            levelUpEvents = levelUpEvents,
            unlockedAffixes = unlockedAffixes,
            updatedAttributes = attributes
        )
    }

    /**
     * 重新计算用户属性面板
     * 遍历所有技能，检查其当前等级是否满足任何 Affix 的 requiredSkillLevel
     */
    suspend fun recalculateAttributes(): Map<AttributeType, Int> {
        val attributes = mutableMapOf<AttributeType, Int>(
            AttributeType.PHYSIQUE to 0,
            AttributeType.INTELLECT to 0,
            AttributeType.CREATION to 0,
            AttributeType.INSIGHT to 0,
            AttributeType.DOMINION to 0
        )

        val allSkills = getAllSkillsSync()
        val allAffixList = getAllAffixesSync()

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

    private suspend fun getAllSkillsSync(): List<Skill> =
        repository.getAllSkills().first()

    private suspend fun getAllAffixesSync(): List<Affix> =
        repository.getAllAffixes().first()
}

/**
 * 视觉等级区间
 */
enum class VisualTier(
    val label: String,
    val colorHex: String,
    val glowHex: String?
) {
    RAW_STONE("原石", "#757575", null),           // 1-9
    BRONZE("青铜", "#B87333", null),               // 10-29
    RED_GOLD("赤金", "#FFBF00", "#FFBF0040"),      // 30-99
    OBSIDIAN("黑曜石", "#1A1A1A", "#00FF7F")       // 100+
}

/**
 * 结算结果
 */
sealed class SettlementResult {
    data class Success(
        val timeRecord: TimeRecord,
        val levelUpEvents: List<LevelUpEvent>,
        val unlockedAffixes: List<Affix>,
        val updatedAttributes: Map<AttributeType, Int>
    ) : SettlementResult()

    data class Error(val message: String) : SettlementResult()
}

/**
 * 升级事件
 */
data class LevelUpEvent(
    val skillId: String,
    val skillName: String,
    val oldLevel: Int,
    val newLevel: Int
)
