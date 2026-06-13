package com.sushi.app

import android.app.Application
import com.sushi.app.data.repository.SeedData
import com.sushi.app.data.repository.SushiRepository
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

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        seedIfFirstLaunch()
    }

    private fun seedIfFirstLaunch() {
        appScope.launch {
            // Fix #7: 真正判断首次启动——通过查询预置技能是否存在
            try {
                val skills = seedData.seedSkills()
                val existingSkill = repository.getSkillById(skills.first().id)
                if (existingSkill == null) {
                    repository.insertSkills(skills)
                    repository.insertProfessions(seedData.seedProfessions())
                    repository.insertAffixes(seedData.seedAffixes())
                    for (task in seedData.seedTasks()) {
                        repository.insertTask(task)
                    }
                    // Fix #10: 预置后同步职业经验值（按关联技能求和）
                    val professions = seedData.seedProfessions()
                    for (profession in professions) {
                        val linkedExp = skills
                            .filter { profession.id in it.linkedProfessionIds }
                            .sumOf { it.totalExp }
                        repository.updateProfessionExp(profession.id, linkedExp)
                    }
                }
            } catch (_: Exception) {
                // 首次启动数据初始化失败不影响应用运行
            }
        }
    }
}
