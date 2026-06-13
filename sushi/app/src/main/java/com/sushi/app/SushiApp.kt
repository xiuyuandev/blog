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
            val count = repository.getTimeRecordCount()
            // 如果时间记录为空且技能也为空，说明是首次启动
            // 使用简单检查：如果没有任何技能数据则预置
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
                }
            } catch (_: Exception) {
                // 首次启动数据初始化失败不影响应用运行
            }
        }
    }
}
