package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TimeRecord(
    @PrimaryKey val id: String,
    val taskId: String,
    val rawDurationMin: Int,
    val netDurationMin: Int,
    val timestamp: Long
)
