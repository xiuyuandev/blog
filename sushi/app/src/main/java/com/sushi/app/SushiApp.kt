package com.sushi.app

import android.app.Application
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 素时 Application
 *
 * 负责:
 * 1. 初始化 [SushiContainer] 顶层单例
 * 2. 首次启动时预置数据(技能/职业/词条/任务/成就/帮助)
 */
class SushiApp : Application() {

    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        SushiContainer.init(this)
        seedIfFirstLaunch()
    }

    private fun seedIfFirstLaunch() {
        appScope.launch {
            try {
                val repository: SushiRepository = SushiContainer.repository
                val seedData = SushiContainer.seedData
                val engine: ExperienceEngine = SushiContainer.engine

                // 技能 / 职业 / 词条 / 任务
                val skills = seedData.seedSkills()
                val existingSkill = repository.getSkillById(skills.first().id)
                if (existingSkill == null) {
                    repository.insertSkills(skills)
                    repository.insertProfessions(seedData.seedProfessions())
                    repository.insertAffixes(seedData.seedAffixes())
                    for (task in seedData.seedTasks()) {
                        repository.insertTask(task)
                    }
                    val professions = seedData.seedProfessions()
                    for (profession in professions) {
                        val linkedExp = skills
                            .filter { profession.id in it.linkedProfessionIds }
                            .sumOf { it.totalExp }
                        repository.updateProfessionExp(profession.id, linkedExp)
                    }
                }
                // 默认成就 + 帮助条目(独立判断,已有则跳过)
                engine.seedDefaultAchievements()
                engine.seedHelpEntries()
            } catch (_: Exception) {
                // 首次启动数据初始化失败不影响应用运行
            }
        }
    }
}
