package com.sushi.app.di

import com.sushi.app.data.repository.SushiRepository
import com.sushi.app.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSushiRepository(
        skillDao: SkillDao,
        professionDao: ProfessionDao,
        affixDao: AffixDao,
        taskDao: TaskDao,
        timeRecordDao: TimeRecordDao
    ): SushiRepository {
        return SushiRepository(skillDao, professionDao, affixDao, taskDao, timeRecordDao)
    }
}
