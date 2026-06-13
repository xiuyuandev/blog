package com.sushi.app

import android.app.Application
import com.sushi.app.data.repository.SeedData
import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.logic.ExperienceEngine
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SushiApp : Application() {

    @Inject lateinit var repository: SushiRepository
    @Inject lateinit var seedData: SeedData
    @Inject lateinit var engine: ExperienceEngine

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        seedIfFirstLaunch()
    }

    private fun seedIfFirstLaunch() {
        appScope.launch {
            // 首次启动预置数据
            try {
                // 技能/职业/词条/任务
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
                // 默认成就 + 帮助条目（独立判断，已有则跳过）
                engine.seedDefaultAchievements()
                engine.seedHelpEntries()
            } catch (_: Exception) {
                // 首次启动数据初始化失败不影响应用运行
            }
        }
    }
}
