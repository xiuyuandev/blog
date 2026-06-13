package com.sushi.app.di

import android.content.Context
import androidx.room.Room
import com.sushi.app.data.dao.*
import com.sushi.app.data.db.SushiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SushiDatabase {
        return Room.databaseBuilder(
            context,
            SushiDatabase::class.java,
            "sushi_database"
        ).build()
    }

    @Provides
    fun provideSkillDao(database: SushiDatabase): SkillDao = database.skillDao()

    @Provides
    fun provideProfessionDao(database: SushiDatabase): ProfessionDao = database.professionDao()

    @Provides
    fun provideAffixDao(database: SushiDatabase): AffixDao = database.affixDao()

    @Provides
    fun provideTaskDao(database: SushiDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideTimeRecordDao(database: SushiDatabase): TimeRecordDao = database.timeRecordDao()
}
