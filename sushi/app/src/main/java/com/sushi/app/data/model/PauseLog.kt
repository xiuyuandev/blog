package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 暂停记录 - 用于分析被打断的模式
 */
@Entity
data class PauseLog(
    @PrimaryKey val id: String,
    val timeRecordId: String,
    val pausedAt: Long,
    val resumedAt: Long?,
    val reason: String,
    val reasonCategory: String  // "FOOD" / "PHONE" / "RESTROOM" / "OTHER"
)
