package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Profession(
    @PrimaryKey val id: String,
    val name: String,
    val totalExp: Int = 0
)
