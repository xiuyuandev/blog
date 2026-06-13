package com.sushi.app.sync

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
 * 同步配置
 */
data class SyncConfig(
    val provider: SyncProvider = SyncProvider.NONE,
    val webDavConfig: WebDavConfig = WebDavConfig(),
    val s3Config: S3Config = S3Config()
)

/**
 * 同步结果
 */
sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
