package com.sushi.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sushi.app.data.dao.*
import com.sushi.app.data.model.*

@Database(
    entities = [Skill::class, Profession::class, Affix::class, Task::class, TimeRecord::class],
    version = 1,
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
}
