package com.sushi.app.di

import com.sushi.app.sync.BackupManager
import com.sushi.app.sync.S3SyncService
import com.sushi.app.sync.SyncConfigManager
import com.sushi.app.sync.WebDavSyncService
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    // SyncConfigManager, BackupManager, WebDavSyncService, S3SyncService
    // are all @Singleton with @Inject constructors, so Hilt provides them automatically.
    // This module exists as a placeholder for future manual bindings if needed.
}
