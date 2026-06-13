package com.sushi.app.data.repository

import com.sushi.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 预置示范数据
 * 首次启动时插入示范技能、职业及词条，确保用户首屏即有数据感知
 */
@Singleton
class SeedData @Inject constructor() {

    fun seedSkills(): List<Skill> = listOf(
        Skill(
            id = "skill_reading",
            name = "阅读",
            category = SkillCategory.COGNITION,
            totalExp = 360, // LV3
            linkedProfessionIds = listOf("prof_digital_nomad")
        ),
        Skill(
            id = "skill_programming",
            name = "编程",
            category = SkillCategory.CREATION,
            totalExp = 1440, // LV12
            linkedProfessionIds = listOf("prof_digital_nomad")
        ),
        Skill(
            id = "skill_meditation",
            name = "冥想",
            category = SkillCategory.STRATEGY,
            totalExp = 120, // LV1
            linkedProfessionIds = listOf("prof_digital_nomad")
        ),
        Skill(
            id = "skill_writing",
            name = "写作",
            category = SkillCategory.CREATION,
            totalExp = 600, // LV5
            linkedProfessionIds = listOf("prof_digital_nomad")
        ),
        Skill(
            id = "skill_exercise",
            name = "锻炼",
            category = SkillCategory.FUNCTION,
            totalExp = 240, // LV2
            linkedProfessionIds = emptyList()
        ),
        Skill(
            id = "skill_language",
            name = "外语",
            category = SkillCategory.COGNITION,
            totalExp = 480, // LV4
            linkedProfessionIds = listOf("prof_digital_nomad")
        ),
        Skill(
            id = "skill_design",
            name = "设计",
            category = SkillCategory.CREATION,
            totalExp = 180, // LV1
            linkedProfessionIds = emptyList()
        ),
        Skill(
            id = "skill_strategy",
            name = "策略思考",
            category = SkillCategory.STRATEGY,
            totalExp = 60, // LV0
            linkedProfessionIds = emptyList()
        )
    )

    fun seedProfessions(): List<Profession> = listOf(
        Profession(
            id = "prof_digital_nomad",
            name = "数字游民",
            totalExp = 3000 // 经验由关联技能同步累加
        )
    )

    fun seedAffixes(): List<Affix> = listOf(
        // 阅读词条
        Affix(
            id = "affix_reading_5",
            name = "速读",
            description = "快速提取文本核心信息的能力",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 2),
            requiredSkillId = "skill_reading",
            requiredSkillLevel = 5
        ),
        Affix(
            id = "affix_reading_10",
            name = "跨文化理解",
            description = "通过广泛阅读获得的跨文化洞察力",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 3, AttributeType.INSIGHT to 2),
            requiredSkillId = "skill_reading",
            requiredSkillLevel = 10
        ),
        Affix(
            id = "affix_reading_20",
            name = "博学者",
            description = "知识的深度积累带来的系统性思维",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 5, AttributeType.INSIGHT to 3, AttributeType.DOMINION to 1),
            requiredSkillId = "skill_reading",
            requiredSkillLevel = 20
        ),
        // 编程词条
        Affix(
            id = "affix_programming_5",
            name = "逻辑构建",
            description = "将复杂问题拆解为可执行步骤的能力",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 2, AttributeType.CREATION to 1),
            requiredSkillId = "skill_programming",
            requiredSkillLevel = 5
        ),
        Affix(
            id = "affix_programming_10",
            name = "架构思维",
            description = "从系统层面设计和优化结构的能力",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 3, AttributeType.CREATION to 3, AttributeType.DOMINION to 1),
            requiredSkillId = "skill_programming",
            requiredSkillLevel = 10
        ),
        Affix(
            id = "affix_programming_20",
            name = "造物主",
            description = "从零创造完整数字世界的能力",
            bonusAttributes = mapOf(AttributeType.CREATION to 5, AttributeType.DOMINION to 3, AttributeType.INSIGHT to 2),
            requiredSkillId = "skill_programming",
            requiredSkillLevel = 20
        ),
        // 冥想词条
        Affix(
            id = "affix_meditation_5",
            name = "内观",
            description = "对自身思维模式的觉察力",
            bonusAttributes = mapOf(AttributeType.INSIGHT to 3),
            requiredSkillId = "skill_meditation",
            requiredSkillLevel = 5
        ),
        Affix(
            id = "affix_meditation_10",
            name = "心如止水",
            description = "在混乱中保持内心平静的能力",
            bonusAttributes = mapOf(AttributeType.INSIGHT to 4, AttributeType.PHYSIQUE to 2),
            requiredSkillId = "skill_meditation",
            requiredSkillLevel = 10
        ),
        // 写作词条
        Affix(
            id = "affix_writing_5",
            name = "表达力",
            description = "将思想转化为清晰文字的能力",
            bonusAttributes = mapOf(AttributeType.CREATION to 2, AttributeType.DOMINION to 1),
            requiredSkillId = "skill_writing",
            requiredSkillLevel = 5
        ),
        Affix(
            id = "affix_writing_10",
            name = "笔锋",
            description = "文字具有影响他人思想的力量",
            bonusAttributes = mapOf(AttributeType.CREATION to 3, AttributeType.DOMINION to 3),
            requiredSkillId = "skill_writing",
            requiredSkillLevel = 10
        ),
        // 锻炼词条
        Affix(
            id = "affix_exercise_5",
            name = "韧体",
            description = "持续锻炼带来的身体韧性",
            bonusAttributes = mapOf(AttributeType.PHYSIQUE to 3),
            requiredSkillId = "skill_exercise",
            requiredSkillLevel = 5
        ),
        Affix(
            id = "affix_exercise_10",
            name = "钢铁意志",
            description = "身体与意志的双重强化",
            bonusAttributes = mapOf(AttributeType.PHYSIQUE to 5, AttributeType.DOMINION to 2),
            requiredSkillId = "skill_exercise",
            requiredSkillLevel = 10
        ),
        // 外语词条
        Affix(
            id = "affix_language_5",
            name = "跨文化沟通",
            description = "用不同语言理解不同世界观",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 3, AttributeType.DOMINION to 2),
            requiredSkillId = "skill_language",
            requiredSkillLevel = 5
        ),
        Affix(
            id = "affix_language_10",
            name = "语言大师",
            description = "语言能力的极致带来思维的多元",
            bonusAttributes = mapOf(AttributeType.INTELLECT to 4, AttributeType.INSIGHT to 3, AttributeType.DOMINION to 2),
            requiredSkillId = "skill_language",
            requiredSkillLevel = 10
        ),
        // 设计词条
        Affix(
            id = "affix_design_5",
            name = "审美直觉",
            description = "对形式与美感的敏锐感知",
            bonusAttributes = mapOf(AttributeType.CREATION to 2, AttributeType.INSIGHT to 2),
            requiredSkillId = "skill_design",
            requiredSkillLevel = 5
        ),
        // 策略思考词条
        Affix(
            id = "affix_strategy_5",
            name = "远见",
            description = "超越当下，看见更长远的格局",
            bonusAttributes = mapOf(AttributeType.INSIGHT to 3, AttributeType.DOMINION to 2),
            requiredSkillId = "skill_strategy",
            requiredSkillLevel = 5
        )
    )

    fun seedTasks(): List<Task> = listOf(
        Task(
            id = "task_deep_reading",
            name = "深度阅读",
            linkedSkillIds = listOf("skill_reading"),
            createdAt = System.currentTimeMillis(),
            isCompleted = false
        ),
        Task(
            id = "task_code_practice",
            name = "编程实践",
            linkedSkillIds = listOf("skill_programming"),
            createdAt = System.currentTimeMillis(),
            isCompleted = false
        ),
        Task(
            id = "task_morning_meditation",
            name = "晨间冥想",
            linkedSkillIds = listOf("skill_meditation"),
            createdAt = System.currentTimeMillis(),
            isCompleted = false
        ),
        Task(
            id = "task_write_journal",
            name = "写日记",
            linkedSkillIds = listOf("skill_writing"),
            createdAt = System.currentTimeMillis(),
            isCompleted = false
        ),
        Task(
            id = "task_workout",
            name = "健身训练",
            linkedSkillIds = listOf("skill_exercise"),
            createdAt = System.currentTimeMillis(),
            isCompleted = false
        ),
        Task(
            id = "task_code_and_read",
            name = "技术文档阅读+实践",
            linkedSkillIds = listOf("skill_programming", "skill_reading"),
            createdAt = System.currentTimeMillis(),
            isCompleted = false
        )
    )
}
