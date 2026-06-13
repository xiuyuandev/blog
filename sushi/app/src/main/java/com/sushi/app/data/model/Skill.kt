package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 技能 - 支持关联前置技能 / 毕业 / 排序
 */
@Entity
data class Skill(
    @PrimaryKey val id: String,
    val name: String,
    val category: SkillCategory,
    val totalExp: Int = 0,
    val linkedProfessionIds: List<String> = emptyList(),
    // #19 前置技能 ID 列表 - 升级到 5 级后前置技能可解锁
    val prerequisiteSkillIds: List<String> = emptyList(),
    // #21 毕业标记 - LV 100+ 可主动毕业
    val isGraduated: Boolean = false,
    // 毕业时间
    val graduatedAt: Long? = null,
    // 毕业寄语
    val graduationMessage: String = "",
    // 排序
    val sortOrder: Int = 0
)

class SkillConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}
