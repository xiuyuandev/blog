package com.sushi.app.sync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_settings")

/**
 * 同步配置管理器(单例,由 [com.sushi.app.SushiContainer] 持有)
 *
 * V1.0 简化:
 * - 移除 S3 配置(国内访问不便)
 * - 移除冲突解决策略(走最简的"远端文件整体覆盖"策略)
 * - 移除白噪音开关(剥离)
 * - 保留 WebDAV(坚果云)配置、主题模式、每日一句、Onboarding 标记
 */
class SyncConfigManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val gson = Gson()

    companion object {
        private val KEY_PROVIDER = stringPreferencesKey("sync_provider")
        private val KEY_WEBDAV_CONFIG = stringPreferencesKey("webdav_config")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DAILY_QUOTE = booleanPreferencesKey("daily_quote_enabled")
        private val KEY_ONBOARDED = booleanPreferencesKey("has_onboarded")
    }

    suspend fun getConfig(): SyncConfig {
        val prefs = appContext.syncDataStore.data.first()
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

        val themeModeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.SYSTEM }

        val dailyQuote = prefs[KEY_DAILY_QUOTE] ?: true
        val onboarded = prefs[KEY_ONBOARDED] ?: false

        return SyncConfig(
            provider = provider,
            webDavConfig = webDavConfig,
            themeMode = themeMode,
            dailyQuoteEnabled = dailyQuote,
            hasOnboarded = onboarded
        )
    }

    fun getConfigFlow() = appContext.syncDataStore.data.map { parseConfig(it) }

    suspend fun saveProvider(provider: SyncProvider) {
        appContext.syncDataStore.edit { it[KEY_PROVIDER] = provider.name }
    }

    suspend fun saveWebDavConfig(config: WebDavConfig) {
        appContext.syncDataStore.edit { it[KEY_WEBDAV_CONFIG] = gson.toJson(config) }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        appContext.syncDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun saveDailyQuoteEnabled(enabled: Boolean) {
        appContext.syncDataStore.edit { it[KEY_DAILY_QUOTE] = enabled }
    }

    suspend fun saveOnboarded(onboarded: Boolean) {
        appContext.syncDataStore.edit { it[KEY_ONBOARDED] = onboarded }
    }
}
