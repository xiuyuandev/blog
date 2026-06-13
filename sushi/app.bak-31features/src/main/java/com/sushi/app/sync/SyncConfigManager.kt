package com.sushi.app.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
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
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_CONFLICT_RESOLUTION = stringPreferencesKey("conflict_resolution")
        private val KEY_WHITE_NOISE = booleanPreferencesKey("white_noise_enabled")
        private val KEY_DAILY_QUOTE = booleanPreferencesKey("daily_quote_enabled")
        private val KEY_ONBOARDED = booleanPreferencesKey("has_onboarded")
    }

    suspend fun getConfig(): SyncConfig {
        val prefs = context.syncDataStore.data.first()
        return parseConfig(prefs)
    }

    private fun parseConfig(prefs: Preferences): SyncConfig {
        val providerStr = prefs[KEY_PROVIDER] ?: SyncProvider.NONE.name
        val provider = try { SyncProvider.valueOf(providerStr) } catch (_: Exception) { SyncProvider.NONE }

        val webDavJson = prefs[KEY_WEBDAV_CONFIG]
        val webDavConfig = if (webDavJson.isNullOrBlank()) {
            WebDavConfig()
        } else {
            try {
                gson.fromJson(webDavJson, WebDavConfig::class.java) ?: WebDavConfig()
            } catch (e: Exception) {
                android.util.Log.e("SyncConfigManager", "WebDavConfig 解析失败: ${e.message}")
                WebDavConfig()
            }
        }

        val s3Json = prefs[KEY_S3_CONFIG]
        val s3Config = if (s3Json.isNullOrBlank()) {
            S3Config()
        } else {
            try {
                gson.fromJson(s3Json, S3Config::class.java) ?: S3Config()
            } catch (e: Exception) {
                android.util.Log.e("SyncConfigManager", "S3Config 解析失败: ${e.message}")
                S3Config()
            }
        }

        val themeModeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.SYSTEM }

        val conflictStr = prefs[KEY_CONFLICT_RESOLUTION] ?: ConflictResolution.ASK_EACH_TIME.name
        val conflictRes = try { ConflictResolution.valueOf(conflictStr) } catch (_: Exception) { ConflictResolution.ASK_EACH_TIME }

        val whiteNoise = prefs[KEY_WHITE_NOISE] ?: false
        val dailyQuote = prefs[KEY_DAILY_QUOTE] ?: true
        val onboarded = prefs[KEY_ONBOARDED] ?: false

        return SyncConfig(
            provider = provider,
            webDavConfig = webDavConfig,
            s3Config = s3Config,
            themeMode = themeMode,
            conflictResolution = conflictRes,
            whiteNoiseEnabled = whiteNoise,
            dailyQuoteEnabled = dailyQuote,
            hasOnboarded = onboarded
        )
    }

    fun getConfigFlow() = context.syncDataStore.data.map { parseConfig(it) }

    suspend fun saveProvider(provider: SyncProvider) {
        context.syncDataStore.edit { it[KEY_PROVIDER] = provider.name }
    }

    suspend fun saveWebDavConfig(config: WebDavConfig) {
        context.syncDataStore.edit { it[KEY_WEBDAV_CONFIG] = gson.toJson(config) }
    }

    suspend fun saveS3Config(config: S3Config) {
        context.syncDataStore.edit { it[KEY_S3_CONFIG] = gson.toJson(config) }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.syncDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun saveConflictResolution(resolution: ConflictResolution) {
        context.syncDataStore.edit { it[KEY_CONFLICT_RESOLUTION] = resolution.name }
    }

    suspend fun saveWhiteNoiseEnabled(enabled: Boolean) {
        context.syncDataStore.edit { it[KEY_WHITE_NOISE] = enabled }
    }

    suspend fun saveDailyQuoteEnabled(enabled: Boolean) {
        context.syncDataStore.edit { it[KEY_DAILY_QUOTE] = enabled }
    }

    suspend fun saveOnboarded(onboarded: Boolean) {
        context.syncDataStore.edit { it[KEY_ONBOARDED] = onboarded }
    }
}
