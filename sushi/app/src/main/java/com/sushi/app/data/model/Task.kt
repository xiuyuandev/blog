package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 任务 - 支持标签/优先级/估算/重复规则/模板/职业专属
 */
@Entity
data class Task(
    @PrimaryKey val id: String,
    val name: String,
    val linkedSkillId: String,
    val createdAt: Long,
    val isCompleted: Boolean = false,
    // #16 标签系统
    val tags: List<String> = emptyList(),
    // #16 任务优先级 0=普通 1=高 2=紧急
    val priority: Int = 0,
    // #18 预估时长（分钟）
    val estimatedDurationMin: Int? = null,
    // #15 重复规则 - null=不重复；"DAILY" / "WEEKLY" / "MONTHLY"
    val recurrenceRule: String? = null,
    // #15 重复任务的父任务 ID（如果本身是某重复任务生成的实例）
    val parentRecurrenceId: String? = null,
    // #17 任务模板标记
    val isTemplate: Boolean = false,
    // #20 职业专属任务 - 关联职业 ID
    val linkedProfessionId: String? = null,
    // 排序权重
    val sortOrder: Int = 0
)
