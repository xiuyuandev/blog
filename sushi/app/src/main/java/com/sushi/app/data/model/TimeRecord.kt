package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimeRecord(
    @PrimaryKey val id: String,
    val skillId: String,         // 关联技能ID
    val taskId: String?,         // 关联任务ID（手动注入时可为null）
    val rawDurationMin: Int,     // 原始时长
    val netDurationMin: Int,     // 纯时间
    val startDateTime: Long,     // 开始时间
    val endDateTime: Long,       // 结束时间
    val description: String,     // 做了什么
    val isManualEntry: Boolean = false,  // 是否手动注入
    val timestamp: Long          // 记录创建时间
)
