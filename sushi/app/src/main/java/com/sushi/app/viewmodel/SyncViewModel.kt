package com.sushi.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.sync.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncUiState(
    val syncConfig: SyncConfig = SyncConfig(),
    val isPushing: Boolean = false,
    val isPulling: Boolean = false,
    val isTesting: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val statusMessage: String = "",
    val lastSyncTime: String = ""
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncConfigManager: SyncConfigManager,
    private val backupManager: BackupManager,
    private val webDavSyncService: WebDavSyncService,
    private val s3SyncService: S3SyncService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            syncConfigManager.getConfigFlow().collect { config ->
                _uiState.update { it.copy(syncConfig = config) }
            }
        }
    }

    // ========== 云端同步 ==========

    fun selectProvider(provider: SyncProvider) {
        viewModelScope.launch {
            syncConfigManager.saveProvider(provider)
        }
    }

    fun saveWebDavConfig(config: WebDavConfig) {
        viewModelScope.launch {
            syncConfigManager.saveWebDavConfig(config)
        }
    }

    fun saveS3Config(config: S3Config) {
        viewModelScope.launch {
            syncConfigManager.saveS3Config(config)
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, statusMessage = "") }
            val config = syncConfigManager.getConfig()

            val result = when (config.provider) {
                SyncProvider.WEBDAV -> {
                    webDavSyncService.setConfig(config.webDavConfig)
                    webDavSyncService.testConnection()
                }
                SyncProvider.S3 -> {
                    s3SyncService.setConfig(config.s3Config)
                    s3SyncService.testConnection()
                }
                else -> SyncResult.Error("请先选择同步方式")
            }

            _uiState.update {
                it.copy(
                    isTesting = false,
                    statusMessage = when (result) {
                        is SyncResult.Success -> "连接成功"
                        is SyncResult.Error -> result.message
                    }
                )
            }
        }
    }

    fun pushToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPushing = true, statusMessage = "") }
            val config = syncConfigManager.getConfig()

            try {
                val json = backupManager.exportToJson()

                val result = when (config.provider) {
                    SyncProvider.WEBDAV -> {
                        webDavSyncService.setConfig(config.webDavConfig)
                        webDavSyncService.push(json)
                    }
                    SyncProvider.S3 -> {
                        s3SyncService.setConfig(config.s3Config)
                        s3SyncService.push(json)
                    }
                    else -> SyncResult.Error("请先选择同步方式")
                }

                _uiState.update {
                    it.copy(
                        isPushing = false,
                        statusMessage = when (result) {
                            is SyncResult.Success -> "推送成功"
                            is SyncResult.Error -> result.message
                        },
                        lastSyncTime = if (result is SyncResult.Success) {
                            java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date())
                        } else it.lastSyncTime
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isPushing = false, statusMessage = "推送失败: ${e.message}")
                }
            }
        }
    }

    fun pullFromCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPulling = true, statusMessage = "") }
            val config = syncConfigManager.getConfig()

            val result = when (config.provider) {
                SyncProvider.WEBDAV -> {
                    webDavSyncService.setConfig(config.webDavConfig)
                    webDavSyncService.pull()
                }
                SyncProvider.S3 -> {
                    s3SyncService.setConfig(config.s3Config)
                    s3SyncService.pull()
                }
                else -> SyncResult.Error("请先选择同步方式")
            }

            when (result) {
                is SyncResult.Success -> {
                    val importResult = backupManager.importFromJson(result.message)
                    _uiState.update {
                        it.copy(
                            isPulling = false,
                            statusMessage = if (importResult.isSuccess) "拉取成功，数据已恢复" else "拉取失败: 数据解析错误",
                            lastSyncTime = if (importResult.isSuccess) {
                                java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date())
                            } else it.lastSyncTime
                        )
                    }
                }
                is SyncResult.Error -> {
                    _uiState.update {
                        it.copy(isPulling = false, statusMessage = result.message)
                    }
                }
            }
        }
    }

    // ========== 本地导出/导入 ==========

    fun exportToFile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, statusMessage = "") }
            try {
                val json = backupManager.exportToJson()
                val fileName = "sushi_backup_${System.currentTimeMillis()}.json"

                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS)
                }

                val uri = context.contentResolver.insert(
                    android.provider.MediaStore.Files.getContentUri("external"),
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                    _uiState.update { it.copy(isExporting = false, statusMessage = "导出成功: $fileName") }
                } else {
                    _uiState.update { it.copy(isExporting = false, statusMessage = "导出失败: 无法创建文件") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, statusMessage = "导出失败: ${e.message}") }
            }
        }
    }

    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, statusMessage = "") }
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { ist ->
                    String(ist.readBytes(), Charsets.UTF_8)
                } ?: throw Exception("无法读取文件")

                val result = backupManager.importFromJson(json)
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        statusMessage = if (result.isSuccess) "导入成功" else "导入失败: 数据格式错误"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, statusMessage = "导入失败: ${e.message}")
                }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = "") }
    }
}
