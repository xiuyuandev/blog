package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日反思 - 柳比歇夫式日记
 */
@Entity
data class DailyReflection(
    @PrimaryKey val id: String,
    val dateKey: String,  // "YYYY-MM-DD"
    val highlight: String = "",    // 今日亮点
    val lesson: String = "",       // 今日所学
    val improvement: String = "",  // 明日可改进
    val moodScore: Int = 3,        // 1-5
    val createdAt: Long = System.currentTimeMillis()
)
