package com.sushi.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sushi.app.sync.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
            val config = withContext(Dispatchers.IO) { syncConfigManager.getConfig() }

            val result = withContext(Dispatchers.IO) {
                when (config.provider) {
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
        // Fix #14: UI 更新在 Main 线程执行，网络 IO 在 IO 线程
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
                        SyncProvider.S3 -> {
                            s3SyncService.setConfig(config.s3Config)
                            s3SyncService.push(json)
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
        // Fix #14: UI 更新在 Main 线程
        viewModelScope.launch {
            _uiState.update { it.copy(isPulling = true, statusMessage = "") }
            val config = withContext(Dispatchers.IO) { syncConfigManager.getConfig() }

            val result = withContext(Dispatchers.IO) {
                when (config.provider) {
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
                            statusMessage = if (importResult.isSuccess) "拉取成功（${if (merge) "合并" else "覆盖"}）" else "拉取失败: 数据解析错误",
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

                val isValid = backupManager.validateJson(json)
                if (!isValid) {
                    _uiState.update { it.copy(isImporting = false, statusMessage = "无效的备份文件格式") }
                    return@launch
                }

                // Show preview instead of importing directly
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
}
