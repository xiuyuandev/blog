package com.sushi.app.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_settings")

@Singleton
class SyncConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        private val KEY_PROVIDER = stringPreferencesKey("sync_provider")
        private val KEY_WEBDAV_CONFIG = stringPreferencesKey("webdav_config")
        private val KEY_S3_CONFIG = stringPreferencesKey("s3_config")
    }

    suspend fun getConfig(): SyncConfig {
        val prefs = context.syncDataStore.data.first()
        val providerStr = prefs[KEY_PROVIDER] ?: SyncProvider.NONE.name
        val provider = try { SyncProvider.valueOf(providerStr) } catch (_: Exception) { SyncProvider.NONE }

        val webDavConfig = try {
            val json = prefs[KEY_WEBDAV_CONFIG] ?: ""
            if (json.isNotBlank()) gson.fromJson(json, WebDavConfig::class.java) else WebDavConfig()
        } catch (_: Exception) { WebDavConfig() }

        val s3Config = try {
            val json = prefs[KEY_S3_CONFIG] ?: ""
            if (json.isNotBlank()) gson.fromJson(json, S3Config::class.java) else S3Config()
        } catch (_: Exception) { S3Config() }

        return SyncConfig(provider, webDavConfig, s3Config)
    }

    fun getConfigFlow() = context.syncDataStore.data.map { prefs ->
        val providerStr = prefs[KEY_PROVIDER] ?: SyncProvider.NONE.name
        val provider = try { SyncProvider.valueOf(providerStr) } catch (_: Exception) { SyncProvider.NONE }

        val webDavConfig = try {
            val json = prefs[KEY_WEBDAV_CONFIG] ?: ""
            if (json.isNotBlank()) gson.fromJson(json, WebDavConfig::class.java) else WebDavConfig()
        } catch (_: Exception) { WebDavConfig() }

        val s3Config = try {
            val json = prefs[KEY_S3_CONFIG] ?: ""
            if (json.isNotBlank()) gson.fromJson(json, S3Config::class.java) else S3Config()
        } catch (_: Exception) { S3Config() }

        SyncConfig(provider, webDavConfig, s3Config)
    }

    suspend fun saveProvider(provider: SyncProvider) {
        context.syncDataStore.edit { it[KEY_PROVIDER] = provider.name }
    }

    suspend fun saveWebDavConfig(config: WebDavConfig) {
        context.syncDataStore.edit { it[KEY_WEBDAV_CONFIG] = gson.toJson(config) }
    }

    suspend fun saveS3Config(config: S3Config) {
        context.syncDataStore.edit { it[KEY_S3_CONFIG] = gson.toJson(config) }
    }
}
