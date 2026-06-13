package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class Affix(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val bonusAttributes: Map<AttributeType, Int>,
    val requiredSkillId: String,
    val requiredSkillLevel: Int
)

class AffixConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromAttributeMap(value: Map<AttributeType, Int>): String = gson.toJson(value)

    @TypeConverter
    fun toAttributeMap(value: String): Map<AttributeType, Int> {
        val type = object : TypeToken<Map<AttributeType, Int>>() {}.type
        return gson.fromJson(value, type)
    }
}
