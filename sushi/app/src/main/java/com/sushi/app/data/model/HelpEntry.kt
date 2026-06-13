package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 帮助中心条目
 */
@Entity
data class HelpEntry(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String,  // "入门" / "概念" / "功能" / "同步" / "故障"
    val sortOrder: Int = 0
)
