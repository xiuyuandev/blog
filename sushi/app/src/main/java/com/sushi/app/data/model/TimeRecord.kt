package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 时间记录 - 支持跨日归属 / 中断次数 / 暂停原因 / 标签 / 全屏模式
 */
@Entity
data class TimeRecord(
    @PrimaryKey val id: String,
    val skillId: String,
    val taskId: String?,
    val rawDurationMin: Int,
    val netDurationMin: Int,
    val startDateTime: Long,
    val endDateTime: Long,
    val description: String = "",
    val isManualEntry: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    // #2 跨日归属 - "YYYY-MM-DD" 格式
    val attributionDateKey: String = "",
    // #11 中断次数（被切走/暂停次数）
    val interruptCount: Int = 0,
    // #12 暂停原因（最后一次暂停的原因）
    val lastPauseReason: String? = null,
    // #14 标签快照（结算时的标签）
    val tagsSnapshot: List<String> = emptyList(),
    // #24 是否为全屏专注模式
    val isFullScreen: Boolean = false
)
