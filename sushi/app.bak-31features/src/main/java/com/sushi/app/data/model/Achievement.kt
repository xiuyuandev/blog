package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 成就 - 解锁条件类型 + 阈值 + 当前进度
 */
@Entity
data class Achievement(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconHint: String,    // 用于显示的图标关键字（如 "STREAK", "TOTAL_TIME", "SKILL_COUNT"）
    val conditionType: String,  // STREAK / TOTAL_TIME / SKILL_COUNT / SKILL_LEVEL / DAILY_REFLECTION / CONSISTENT_WEEK
    val threshold: Int,
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val tierLabel: String = "铜"  // 铜/银/金
)
