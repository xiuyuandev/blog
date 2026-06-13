package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 目标 - 周/月度纯时间目标
 */
@Entity
data class Goal(
    @PrimaryKey val id: String,
    val name: String,
    val period: String,  // "WEEKLY" / "MONTHLY" / "CUSTOM"
    val targetMinutes: Int,
    val scopeSkillId: String? = null,  // null=全部技能
    val startTime: Long,
    val endTime: Long,
    val currentMinutes: Int = 0,
    val isActive: Boolean = true
)
