package com.sushi.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sushi.app.data.dao.*
import com.sushi.app.data.model.*

@Database(
    entities = [
        Skill::class,
        Profession::class,
        Affix::class,
        Task::class,
        TimeRecord::class,
        Achievement::class,
        Goal::class,
        DailyReflection::class,
        PauseLog::class,
        SyncConflict::class,
        HelpEntry::class
    ],
    version = 3,
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
    abstract fun achievementDao(): AchievementDao
    abstract fun goalDao(): GoalDao
    abstract fun dailyReflectionDao(): DailyReflectionDao
    abstract fun pauseLogDao(): PauseLogDao
    abstract fun syncConflictDao(): SyncConflictDao
    abstract fun helpEntryDao(): HelpEntryDao

    companion object {
        // v1 → v2: 占位迁移
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema 无变更
            }
        }

        // v2 → v3: 添加 6 张新表 + 扩展字段
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Skill 扩展字段
                db.execSQL("ALTER TABLE Skill ADD COLUMN prerequisiteSkillIds TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE Skill ADD COLUMN isGraduated INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE Skill ADD COLUMN graduatedAt INTEGER")
                db.execSQL("ALTER TABLE Skill ADD COLUMN graduationMessage TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE Skill ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")

                // Task 扩展字段
                db.execSQL("ALTER TABLE Task ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE Task ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE Task ADD COLUMN estimatedDurationMin INTEGER")
                db.execSQL("ALTER TABLE Task ADD COLUMN recurrenceRule TEXT")
                db.execSQL("ALTER TABLE Task ADD COLUMN parentRecurrenceId TEXT")
                db.execSQL("ALTER TABLE Task ADD COLUMN isTemplate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE Task ADD COLUMN linkedProfessionId TEXT")
                db.execSQL("ALTER TABLE Task ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")

                // TimeRecord 扩展字段
                db.execSQL("ALTER TABLE TimeRecord ADD COLUMN attributionDateKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE TimeRecord ADD COLUMN interruptCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE TimeRecord ADD COLUMN lastPauseReason TEXT")
                db.execSQL("ALTER TABLE TimeRecord ADD COLUMN tagsSnapshot TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE TimeRecord ADD COLUMN isFullScreen INTEGER NOT NULL DEFAULT 0")

                // 新表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS Achievement (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        iconHint TEXT NOT NULL,
                        conditionType TEXT NOT NULL,
                        threshold INTEGER NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        unlockedAt INTEGER,
                        tierLabel TEXT NOT NULL DEFAULT '铜'
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS Goal (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        period TEXT NOT NULL,
                        targetMinutes INTEGER NOT NULL,
                        scopeSkillId TEXT,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        currentMinutes INTEGER NOT NULL DEFAULT 0,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS DailyReflection (
                        id TEXT NOT NULL PRIMARY KEY,
                        dateKey TEXT NOT NULL,
                        highlight TEXT NOT NULL DEFAULT '',
                        lesson TEXT NOT NULL DEFAULT '',
                        improvement TEXT NOT NULL DEFAULT '',
                        moodScore INTEGER NOT NULL DEFAULT 3,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS PauseLog (
                        id TEXT NOT NULL PRIMARY KEY,
                        timeRecordId TEXT NOT NULL,
                        pausedAt INTEGER NOT NULL,
                        resumedAt INTEGER,
                        reason TEXT NOT NULL,
                        reasonCategory TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS SyncConflict (
                        id TEXT NOT NULL PRIMARY KEY,
                        entityType TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        localUpdatedAt INTEGER NOT NULL,
                        remoteUpdatedAt INTEGER NOT NULL,
                        localData TEXT NOT NULL,
                        remoteData TEXT NOT NULL,
                        resolution TEXT NOT NULL DEFAULT 'PENDING'
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS HelpEntry (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        category TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
