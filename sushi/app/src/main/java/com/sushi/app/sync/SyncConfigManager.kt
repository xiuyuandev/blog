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
        return parseConfig(prefs)
    }

    private fun parseConfig(prefs: androidx.datastore.preferences.core.Preferences): SyncConfig {
        val providerStr = prefs[KEY_PROVIDER] ?: SyncProvider.NONE.name
        val provider = try { SyncProvider.valueOf(providerStr) } catch (_: Exception) { SyncProvider.NONE }

        // Fix #8: 解析失败时返回默认空配置，但不让原始 JSON 丢失
        // 保留原始 JSON 字符串，以便后续修复或排查
        val webDavJson = prefs[KEY_WEBDAV_CONFIG]
        val webDavConfig = if (webDavJson.isNullOrBlank()) {
            WebDavConfig()
        } else {
            try {
                gson.fromJson(webDavJson, WebDavConfig::class.java) ?: WebDavConfig()
            } catch (e: Exception) {
                // Fix #8: 解析失败时不覆盖原数据，保留原始 JSON 以便排查
                android.util.Log.e("SyncConfigManager", "WebDavConfig 解析失败，保留原始数据: ${e.message}")
                WebDavConfig(serverUrl = "__PARSE_ERROR__", username = "", password = "", remotePath = "")
            }
        }

        val s3Json = prefs[KEY_S3_CONFIG]
        val s3Config = if (s3Json.isNullOrBlank()) {
            S3Config()
        } else {
            try {
                gson.fromJson(s3Json, S3Config::class.java) ?: S3Config()
            } catch (e: Exception) {
                android.util.Log.e("SyncConfigManager", "S3Config 解析失败，保留原始数据: ${e.message}")
                S3Config(endpoint = "__PARSE_ERROR__", region = "", bucket = "", accessKey = "", secretKey = "", remotePath = "")
            }
        }

        return SyncConfig(provider, webDavConfig, s3Config)
    }

    fun getConfigFlow() = context.syncDataStore.data.map { prefs ->
        parseConfig(prefs)
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
