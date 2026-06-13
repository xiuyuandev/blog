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
 */
enum class SyncProvider {
    NONE,       // 未配置
    WEBDAV,     // 坚果云 WebDAV
    S3          // 七牛云 S3
}

/**
 * WebDAV 配置（坚果云）
 */
data class WebDavConfig(
    val serverUrl: String = "",     // 如 https://dav.jianguoyun.com/dav/
    val username: String = "",
    val password: String = "",      // 坚果云应用专用密码
    val remotePath: String = "/sushi/" // 远程目录路径
)

/**
 * S3 配置（七牛云）
 */
data class S3Config(
    val endpoint: String = "",      // 如 s3-cn-south-1.qiniucs.com
    val region: String = "",        // 如 cn-south-1
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val remotePath: String = "sushi/" // 远程对象前缀
)

/**
 * 同步冲突解决策略
 */
enum class ConflictResolution {
    ASK_EACH_TIME,    // 每次询问
    LOCAL_WINS,       // 本地优先
    REMOTE_WINS,      // 远端优先
    ALWAYS_MERGE      // 总是合并
}

/**
 * 同步配置
 */
data class SyncConfig(
    val provider: SyncProvider = SyncProvider.NONE,
    val webDavConfig: WebDavConfig = WebDavConfig(),
    val s3Config: S3Config = S3Config(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val conflictResolution: ConflictResolution = ConflictResolution.ASK_EACH_TIME,
    val whiteNoiseEnabled: Boolean = false,
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
