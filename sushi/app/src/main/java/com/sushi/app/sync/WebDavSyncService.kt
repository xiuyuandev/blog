package com.sushi.app.sync

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

/**
 * WebDAV 同步实现(坚果云)
 *
 * 坚果云 WebDAV 基本操作:
 * - MKCOL: 创建远程目录
 * - PUT: 上传文件
 * - GET: 下载文件
 * - PROPFIND: 检查文件/目录是否存在
 *
 * 由 [com.sushi.app.SushiContainer] 持有单例,不再依赖 Hilt。
 */
class WebDavSyncService : SyncService {

    @Volatile
    private var currentConfig: WebDavConfig? = null

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val xmlMediaType = "application/xml; charset=utf-8".toMediaType()

    companion object {
        private const val BACKUP_FILENAME = "sushi_backup.json"
        // 备份文件大小上限 10 MB,防止恶意/异常大文件 OOM
        private const val MAX_BACKUP_SIZE = 10L * 1024 * 1024
    }

    override suspend fun push(jsonContent: String): SyncResult {
        return try {
            val config = currentConfig ?: return SyncResult.Error("WebDAV 未配置")

            // 确保远程目录存在
            ensureDirectory(config)

            // 上传备份文件
            val url = buildUrl(config.serverUrl, config.remotePath, BACKUP_FILENAME)
            val credential = Credentials.basic(config.username, config.password)

            val body = jsonContent.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .put(body)
                .header("Authorization", credential)
                .build()

            // use 块确保 response 关闭,避免连接池泄漏
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    SyncResult.Success("推送成功")
                } else {
                    SyncResult.Error("推送失败: HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            SyncResult.Error("推送失败: ${e.message}")
        }
    }

    override suspend fun pull(): SyncResult {
        return try {
            val config = currentConfig ?: return SyncResult.Error("WebDAV 未配置")

            val url = buildUrl(config.serverUrl, config.remotePath, BACKUP_FILENAME)
            val credential = Credentials.basic(config.username, config.password)

            val request = Request.Builder()
                .url(url)
                .get()
                .header("Authorization", credential)
                .build()

            client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        // Fix #15: 检查 Content-Length,超过限制直接拒绝
                        val contentLength = response.body?.contentLength() ?: -1L
                        if (contentLength > MAX_BACKUP_SIZE) {
                            return SyncResult.Error(
                                "备份文件过大 (${contentLength / 1024} KB),已超过 10 MB 限制"
                            )
                        }

                        val body = response.body?.string()
                            ?: return SyncResult.Error("拉取失败: 响应为空")
                        // 二次保护:实际读取超过限制也要拒绝
                        if (body.length.toLong() > MAX_BACKUP_SIZE) {
                            return SyncResult.Error("备份文件过大,已超过 10 MB 限制")
                        }
                        SyncResult.Success(body)
                    }
                    response.code == 404 -> SyncResult.Error("云端暂无备份数据")
                    else -> SyncResult.Error("拉取失败: HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            SyncResult.Error("拉取失败: ${e.message}")
        }
    }

    override suspend fun testConnection(): SyncResult {
        return try {
            val config = currentConfig ?: return SyncResult.Error("WebDAV 未配置")

            val url = config.serverUrl.trimEnd('/') + "/"
            val credential = Credentials.basic(config.username, config.password)

            val propfindBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <propfind xmlns="DAV:"><prop></prop></propfind>
            """.trimIndent()

            val body = propfindBody.toRequestBody(xmlMediaType)
            val request = Request.Builder()
                .url(url)
                .method("PROPFIND", body)
                .header("Authorization", credential)
                .header("Depth", "0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 207) {
                    SyncResult.Success("连接成功")
                } else {
                    SyncResult.Error("连接失败: HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            SyncResult.Error("连接失败: ${e.message}")
        }
    }

    fun setConfig(config: WebDavConfig) {
        currentConfig = config
    }

    private fun ensureDirectory(config: WebDavConfig) {
        try {
            val url = config.serverUrl.trimEnd('/') + config.remotePath
            val credential = Credentials.basic(config.username, config.password)

            val request = Request.Builder()
                .url(url)
                .method("MKCOL", null)
                .header("Authorization", credential)
                .build()

            // use 块确保关闭
            client.newCall(request).execute().use { /* 201/405/409 都视为成功 */ }
            // 201 = created, 405/409 = already exists, both are fine
        } catch (_: Exception) {
            // 目录创建失败不阻塞上传
        }
    }

    private fun buildUrl(serverUrl: String, remotePath: String, filename: String): String {
        val base = serverUrl.trimEnd('/')
        val path = remotePath.trimEnd('/')
        return "$base$path/${URLEncoder.encode(filename, "UTF-8")}"
    }
}
