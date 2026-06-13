package com.sushi.app.sync

/**
 * 主题模式
 */
enum class ThemeMode {
    SYSTEM,  // 跟随系统
    LIGHT,   // 强制浅色
    DARK     // 强制深色
}

/**
 * 同步服务类型
 *
 * V1.0 精简:只保留 NONE 和 WEBDAV(坚果云)
 * S3 已剥离(国内访问不便)
 */
enum class SyncProvider {
    NONE,       // 未配置
    WEBDAV      // 坚果云 WebDAV
}

/**
 * WebDAV 配置(坚果云)
 */
data class WebDavConfig(
    val serverUrl: String = "https://dav.jianguoyun.com/dav/",  // 坚果云默认 WebDAV 地址
    val username: String = "",
    val password: String = "",       // 坚果云应用专用密码
    val remotePath: String = "/sushi/"  // 远程目录路径
)

/**
 * 同步配置
 */
data class SyncConfig(
    val provider: SyncProvider = SyncProvider.NONE,
    val webDavConfig: WebDavConfig = WebDavConfig(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dailyQuoteEnabled: Boolean = true,
    val hasOnboarded: Boolean = false
)

/**
 * 同步结果
 */
sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class ConflictsDetected(val conflictCount: Int) : SyncResult()
}
