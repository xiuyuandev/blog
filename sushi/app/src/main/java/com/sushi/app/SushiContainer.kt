package com.sushi.app

import android.content.Context
import androidx.room.Room
import com.sushi.app.data.db.SushiDatabase
import com.sushi.app.data.repository.SeedData
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import com.sushi.app.sync.SyncConfigManager
import com.sushi.app.sync.WebDavSyncService

/**
 * 顶层单例容器,替代 Hilt。
 *
 * 初始化时机:在 [SushiApp.onCreate] 中调用 [SushiContainer.init]。
 * 之后任何位置可通过 `SushiContainer.repository` / `SushiContainer.engine` 等访问。
 *
 * 设计原则:
 * - 单例(Scala object),整个 App 进程唯一
 * - lateinit var,初始化后才能使用
 * - 同步访问:用 @Volatile 保证可见性
 */
object SushiContainer {

    @Volatile
    lateinit var database: SushiDatabase
        private set

    @Volatile
    lateinit var repository: SushiRepository
        private set

    @Volatile
    lateinit var engine: ExperienceEngine
        private set

    @Volatile
    lateinit var seedData: SeedData
        private set

    @Volatile
    lateinit var syncConfigManager: SyncConfigManager
        private set

    @Volatile
    lateinit var webDavSyncService: WebDavSyncService
        private set

    @Volatile
    private var initialized: Boolean = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val appContext = context.applicationContext
            database = Room.databaseBuilder(
                appContext,
                SushiDatabase::class.java,
                "sushi_database"
            ).addMigrations(
                SushiDatabase.MIGRATION_1_2,
                SushiDatabase.MIGRATION_2_3
            ).fallbackToDestructiveMigration().build()

            repository = SushiRepository(
                skillDao = database.skillDao(),
                professionDao = database.professionDao(),
                affixDao = database.affixDao(),
                taskDao = database.taskDao(),
                timeRecordDao = database.timeRecordDao(),
                achievementDao = database.achievementDao(),
                goalDao = database.goalDao(),
                dailyReflectionDao = database.dailyReflectionDao(),
                pauseLogDao = database.pauseLogDao(),
                syncConflictDao = database.syncConflictDao(),
                helpEntryDao = database.helpEntryDao()
            )
            seedData = SeedData()
            engine = ExperienceEngine(repository)
            syncConfigManager = SyncConfigManager(appContext)
            webDavSyncService = WebDavSyncService()
            initialized = true
        }
    }

    /**
     * 测试用:重置所有引用。
     *
     * 注意:此方法必须从测试上下文中调用,生产环境调用会立即抛错。
     * 不在 `synchronized` 块内 throw,以确保错误信息在调用栈中清晰可见。
     */
    internal fun resetForTest() {
        throw IllegalStateException(
            "SushiContainer.resetForTest 必须在测试上下文中调用," +
                "并通过反射或依赖注入替换 SushiContainer 的内部状态"
        )
    }
}
