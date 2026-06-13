package com.sushi.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.SushiContainer
import com.sushi.app.sync.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SyncUiState(
    val syncConfig: SyncConfig = SyncConfig(),
    val isPushing: Boolean = false,
    val isPulling: Boolean = false,
    val isTesting: Boolean = false,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val statusMessage: String = "",
    val lastSyncTime: String = "",
    val showPullConfirmation: Boolean = false,
    val showImportConfirmation: Boolean = false,
    val pendingImportJson: String? = null,
    val lastPushTimestamp: Long = 0L,
    val showExportFormatDialog: Boolean = false,
    val showWebDavConfigDialog: Boolean = false,
    val pendingExportJson: String? = null,
    val pendingExportCsv: String? = null,
    val pendingExportFileName: String = ""
)

/**
 * 同步 ViewModel(V1.0 精简版)
 *
 * 移除 S3、ConflictResolution、WhiteNoise。
 * 保留 WebDAV 同步、本地 JSON/CSV 导入导出、主题模式、每日一句。
 */
class SyncViewModel(
    private val syncConfigManager: SyncConfigManager = SushiContainer.syncConfigManager,
    private val backupManager: BackupManager = BackupManager(SushiContainer.repository),
    private val webDavSyncService: WebDavSyncService = SushiContainer.webDavSyncService
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

    // ========== 云端同步(仅 WebDAV) ==========

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

    fun showWebDavConfigDialog() {
        _uiState.update { it.copy(showWebDavConfigDialog = true) }
    }

    fun hideWebDavConfigDialog() {
        _uiState.update { it.copy(showWebDavConfigDialog = false) }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, statusMessage = "") }
            val config = withContext(Dispatchers.IO) { syncConfigManager.getConfig() }

            val result = withContext(Dispatchers.IO) {
                when (config.provider) {
                    SyncProvider.WEBDAV -> {
                        webDavSyncService.setConfig(config.webDavConfig)
                        webDavSyncService.testConnection()
                    }
                    else -> SyncResult.Error("请先选择同步方式")
                }
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
            val config = withContext(Dispatchers.IO) { syncConfigManager.getConfig() }

            try {
                val (result, pushTime) = withContext(Dispatchers.IO) {
                    val json = backupManager.exportToJson()
                    val r = when (config.provider) {
                        SyncProvider.WEBDAV -> {
                            webDavSyncService.setConfig(config.webDavConfig)
                            webDavSyncService.push(json)
                        }
                        else -> SyncResult.Error("请先选择同步方式")
                    }
                    r to System.currentTimeMillis()
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
                        } else it.lastSyncTime,
                        lastPushTimestamp = if (result is SyncResult.Success) pushTime else it.lastPushTimestamp
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isPushing = false, statusMessage = "推送失败: ${e.message}")
                }
            }
        }
    }

    fun pullFromCloud(merge: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPulling = true, statusMessage = "") }
            val config = withContext(Dispatchers.IO) { syncConfigManager.getConfig() }

            val result = withContext(Dispatchers.IO) {
                when (config.provider) {
                    SyncProvider.WEBDAV -> {
                        webDavSyncService.setConfig(config.webDavConfig)
                        webDavSyncService.pull()
                    }
                    else -> SyncResult.Error("请先选择同步方式")
                }
            }

            when (result) {
                is SyncResult.Success -> {
                    val importResult = withContext(Dispatchers.IO) {
                        if (merge) {
                            try {
                                backupManager.mergeFromJson(result.message)
                                Result.success(Unit)
                            } catch (e: Exception) {
                                Result.failure(e)
                            }
                        } else {
                            backupManager.importFromJson(result.message)
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isPulling = false,
                            statusMessage = if (importResult.isSuccess) "拉取成功(${if (merge) "合并" else "覆盖"})" else "拉取失败: 数据解析错误",
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

    // ========== 本地导入(由 Composable 提供 contentResolver) ==========

    fun importFromFile(contentResolver: android.content.ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, statusMessage = "") }
            try {
                val json = contentResolver.openInputStream(uri)?.use { ist ->
                    String(ist.readBytes(), Charsets.UTF_8)
                } ?: throw Exception("无法读取文件")

                val isValid = backupManager.validateJson(json)
                if (!isValid) {
                    _uiState.update { it.copy(isImporting = false, statusMessage = "无效的备份文件格式") }
                    return@launch
                }
                _uiState.update { it.copy(isImporting = false) }
                requestImport(json)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, statusMessage = "读取文件失败: ${e.message}")
                }
            }
        }
    }

    fun requestPull() {
        _uiState.update { it.copy(showPullConfirmation = true) }
    }

    fun confirmPull() {
        _uiState.update { it.copy(showPullConfirmation = false) }
        pullFromCloud(merge = false)
    }

    fun confirmPullMerge() {
        _uiState.update { it.copy(showPullConfirmation = false) }
        pullFromCloud(merge = true)
    }

    fun cancelPull() {
        _uiState.update { it.copy(showPullConfirmation = false) }
    }

    fun requestImport(json: String) {
        _uiState.update { it.copy(showImportConfirmation = true, pendingImportJson = json) }
    }

    fun confirmImport() {
        val json = _uiState.value.pendingImportJson ?: return
        _uiState.update { it.copy(showImportConfirmation = false, pendingImportJson = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val isValid = backupManager.validateJson(json)
                if (!isValid) {
                    _uiState.update { it.copy(isImporting = false, statusMessage = "无效的备份文件格式") }
                    return@launch
                }
                backupManager.importFromJson(json)
                _uiState.update { it.copy(isImporting = false, statusMessage = "导入成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, statusMessage = "导入失败: ${e.message}") }
            }
        }
    }

    fun cancelImport() {
        _uiState.update { it.copy(showImportConfirmation = false, pendingImportJson = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = "") }
    }

    // ========== 偏好设置 ==========

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            syncConfigManager.saveThemeMode(mode)
        }
    }

    fun setDailyQuoteEnabled(enabled: Boolean) {
        viewModelScope.launch {
            syncConfigManager.saveDailyQuoteEnabled(enabled)
        }
    }

    // ========== 导出(由 Composable 触发 SAF 写入) ==========

    fun showExportFormatDialog() {
        _uiState.update { it.copy(showExportFormatDialog = true) }
    }

    fun hideExportFormatDialog() {
        _uiState.update { it.copy(showExportFormatDialog = false) }
    }

    fun onSelectJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(showExportFormatDialog = false) }
            val json = withContext(Dispatchers.IO) { backupManager.exportToJson() }
            _uiState.update {
                it.copy(
                    pendingExportJson = json,
                    pendingExportFileName = "sushi_backup_${System.currentTimeMillis()}.json"
                )
            }
        }
    }

    fun onSelectCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(showExportFormatDialog = false) }
            val csv = withContext(Dispatchers.IO) { backupManager.exportTimeRecordsToCsv() }
            _uiState.update {
                it.copy(
                    pendingExportCsv = csv,
                    pendingExportFileName = "sushi_records_${System.currentTimeMillis()}.csv"
                )
            }
        }
    }

    fun consumePendingExport() {
        _uiState.update { it.copy(pendingExportJson = null, pendingExportCsv = null) }
    }

    /**
     * 由 Composable 实际写入文件(使用 SAF / MediaStore)
     * 返回成功/失败消息
     */
    fun writeExportFile(
        contentResolver: android.content.ContentResolver,
        json: String?,
        csv: String?,
        fileName: String
    ) {
        viewModelScope.launch {
            try {
                val (content, mime) = when {
                    json != null -> json to "application/json"
                    csv != null -> csv to "text/csv"
                    else -> {
                        _uiState.update { it.copy(statusMessage = "导出失败: 无内容") }
                        return@launch
                    }
                }
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOCUMENTS)
                }
                val uri = contentResolver.insert(
                    android.provider.MediaStore.Files.getContentUri("external"),
                    contentValues
                )
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(content.toByteArray(Charsets.UTF_8))
                    }
                    _uiState.update { it.copy(statusMessage = "导出成功: $fileName") }
                } else {
                    _uiState.update { it.copy(statusMessage = "导出失败: 无法创建文件") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "导出失败: ${e.message}") }
            }
        }
    }
}
