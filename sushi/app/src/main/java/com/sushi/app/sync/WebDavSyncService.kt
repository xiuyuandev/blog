package com.sushi.app.sync

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

/**
 * WebDAV 同步实现（坚果云）
 *
 * 坚果云 WebDAV 基本操作：
 * - MKCOL: 创建远程目录
 * - PUT: 上传文件
 * - GET: 下载文件
 * - PROPFIND: 检查文件/目录是否存在
 */
@Singleton
class WebDavSyncService @Inject constructor() : SyncService {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val BACKUP_FILENAME = "sushi_backup.json"
    }

    override suspend fun push(jsonContent: String): SyncResult {
        return try {
            val config = getCurrentConfig() ?: return SyncResult.Error("WebDAV 未配置")

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

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                SyncResult.Success("推送成功")
            } else {
                SyncResult.Error("推送失败: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SyncResult.Error("推送失败: ${e.message}")
        }
    }

    override suspend fun pull(): SyncResult {
        return try {
            val config = getCurrentConfig() ?: return SyncResult.Error("WebDAV 未配置")

            val url = buildUrl(config.serverUrl, config.remotePath, BACKUP_FILENAME)
            val credential = Credentials.basic(config.username, config.password)

            val request = Request.Builder()
                .url(url)
                .get()
                .header("Authorization", credential)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return SyncResult.Error("拉取失败: 响应为空")
                SyncResult.Success(body)
            } else if (response.code == 404) {
                SyncResult.Error("云端暂无备份数据")
            } else {
                SyncResult.Error("拉取失败: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SyncResult.Error("拉取失败: ${e.message}")
        }
    }

    override suspend fun testConnection(): SyncResult {
        return try {
            val config = getCurrentConfig() ?: return SyncResult.Error("WebDAV 未配置")

            val url = config.serverUrl.trimEnd('/') + "/"
            val credential = Credentials.basic(config.username, config.password)

            val propfindBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <propfind xmlns="DAV:"><prop></prop></propfind>
            """.trimIndent()

            val body = propfindBody.toRequestBody("application/xml; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .method("PROPFIND", body)
                .header("Authorization", credential)
                .header("Depth", "0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 207) {
                SyncResult.Success("连接成功")
            } else {
                SyncResult.Error("连接失败: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SyncResult.Error("连接失败: ${e.message}")
        }
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

            client.newCall(request).execute().close()
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

    private var currentConfig: WebDavConfig? = null

    fun setConfig(config: WebDavConfig) {
        currentConfig = config
    }

    private fun getCurrentConfig(): WebDavConfig? = currentConfig
}
