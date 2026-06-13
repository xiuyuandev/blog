package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey val id: String,
    val name: String,
    val linkedSkillIds: List<String>,
    val createdAt: Long,
    val isCompleted: Boolean = false
)
