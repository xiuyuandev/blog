package com.sushi.app.data.db

import androidx.room.Database
import androidx.room.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sushi.app.data.dao.*
import com.sushi.app.data.model.*

@Database(
    entities = [Skill::class, Profession::class, Affix::class, Task::class, TimeRecord::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    SkillConverters::class,
    AffixConverters::class
)
abstract class SushiDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDao
    abstract fun professionDao(): ProfessionDao
    abstract fun affixDao(): AffixDao
    abstract fun taskDao(): TaskDao
    abstract fun timeRecordDao(): TimeRecordDao

    companion object {
        // Fix #11: v1→v2 主要是 DAO 方法签名/返回类型变更，无 schema 变更。
        // 保持占位迁移以确保已有用户的数据库能从 v1 顺利升到 v2，
        // Room 会重新编译所有预编译 SQL 声明。
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema 无变更，此迁移为兼容性占位
            }
        }
    }
}
