package com.sushi.app.sync

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * S3 兼容同步实现（七牛云等）
 *
 * 使用 AWS Signature V4 签名，兼容所有 S3 协议存储
 */
@Singleton
class S3SyncService @Inject constructor() : SyncService {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    companion object {
        private const val BACKUP_KEY = "sushi_backup.json"
        private const val SERVICE = "s3"
        private const val AWS4_REQUEST = "aws4_request"
        private const val ALGORITHM = "AWS4-HMAC-SHA256"
        // Fix #15: 备份文件大小上限 10 MB，防止恶意/异常大文件 OOM
        private const val MAX_BACKUP_SIZE = 10L * 1024 * 1024
    }

    override suspend fun push(jsonContent: String): SyncResult {
        return try {
            val config = getCurrentConfig() ?: return SyncResult.Error("S3 未配置")

            val objectKey = buildObjectKey(config.remotePath)
            val url = "https://${config.bucket}.${config.endpoint}/${objectKey}"

            val now = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val dateStamp = now.substring(0, 8)
            val payloadHash = sha256Hex(jsonContent.toByteArray())

            val host = "${config.bucket}.${config.endpoint}"

            val canonicalHeaders = "content-type:application/json\nhost:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$now\n"
            val signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date"

            val canonicalRequest = "PUT\n/${objectKey}\n\n$canonicalHeaders\n$signedHeaders\n$payloadHash"

            val credentialScope = "$dateStamp/${config.region}/$SERVICE/$AWS4_REQUEST"
            val stringToSign = "$ALGORITHM\n$now\n$credentialScope\n${sha256Hex(canonicalRequest.toByteArray())}"

            val signingKey = getSignatureKey(config.secretKey, dateStamp, config.region, SERVICE)
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authorization = "$ALGORITHM Credential=${config.accessKey}/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

            val body = jsonContent.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .put(body)
                .header("Host", host)
                .header("Content-Type", "application/json")
                .header("X-Amz-Date", now)
                .header("X-Amz-Content-Sha256", payloadHash)
                .header("Authorization", authorization)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                SyncResult.Success("推送成功")
            } else {
                val errorBody = response.body?.string()?.take(200) ?: ""
                SyncResult.Error("推送失败: HTTP ${response.code} $errorBody")
            }
        } catch (e: Exception) {
            SyncResult.Error("推送失败: ${e.message}")
        }
    }

    override suspend fun pull(): SyncResult {
        return try {
            val config = getCurrentConfig() ?: return SyncResult.Error("S3 未配置")

            val objectKey = buildObjectKey(config.remotePath)
            val url = "https://${config.bucket}.${config.endpoint}/${objectKey}"

            val now = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val dateStamp = now.substring(0, 8)
            val payloadHash = "UNSIGNED-PAYLOAD"

            val host = "${config.bucket}.${config.endpoint}"

            val canonicalHeaders = "host:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$now\n"
            val signedHeaders = "host;x-amz-content-sha256;x-amz-date"

            val canonicalRequest = "GET\n/${objectKey}\n\n$canonicalHeaders\n$signedHeaders\n$payloadHash"

            val credentialScope = "$dateStamp/${config.region}/$SERVICE/$AWS4_REQUEST"
            val stringToSign = "$ALGORITHM\n$now\n$credentialScope\n${sha256Hex(canonicalRequest.toByteArray())}"

            val signingKey = getSignatureKey(config.secretKey, dateStamp, config.region, SERVICE)
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authorization = "$ALGORITHM Credential=${config.accessKey}/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

            val request = Request.Builder()
                .url(url)
                .get()
                .header("Host", host)
                .header("X-Amz-Date", now)
                .header("X-Amz-Content-Sha256", payloadHash)
                .header("Authorization", authorization)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Fix #15: 检查 Content-Length，超过限制直接拒绝
                val contentLength = response.body?.contentLength() ?: -1L
                if (contentLength > MAX_BACKUP_SIZE) {
                    response.close()
                    return SyncResult.Error("备份文件过大 (${contentLength / 1024} KB)，已超过 10 MB 限制")
                }

                val body = response.body?.string() ?: return SyncResult.Error("拉取失败: 响应为空")
                if (body.length.toLong() > MAX_BACKUP_SIZE) {
                    return SyncResult.Error("备份文件过大，已超过 10 MB 限制")
                }
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
            val config = getCurrentConfig() ?: return SyncResult.Error("S3 未配置")

            // 使用 HEAD bucket 测试连接
            val url = "https://${config.bucket}.${config.endpoint}/"

            val now = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val dateStamp = now.substring(0, 8)
            val payloadHash = "UNSIGNED-PAYLOAD"

            val host = "${config.bucket}.${config.endpoint}"

            val canonicalHeaders = "host:$host\nx-amz-content-sha256:$payloadHash\nx-amz-date:$now\n"
            val signedHeaders = "host;x-amz-content-sha256;x-amz-date"

            val canonicalRequest = "HEAD\n/\n\n$canonicalHeaders\n$signedHeaders\n$payloadHash"

            val credentialScope = "$dateStamp/${config.region}/$SERVICE/$AWS4_REQUEST"
            val stringToSign = "$ALGORITHM\n$now\n$credentialScope\n${sha256Hex(canonicalRequest.toByteArray())}"

            val signingKey = getSignatureKey(config.secretKey, dateStamp, config.region, SERVICE)
            val signature = hmacSha256Hex(signingKey, stringToSign)

            val authorization = "$ALGORITHM Credential=${config.accessKey}/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"

            val request = Request.Builder()
                .url(url)
                .head()
                .header("Host", host)
                .header("X-Amz-Date", now)
                .header("X-Amz-Content-Sha256", payloadHash)
                .header("Authorization", authorization)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                SyncResult.Success("连接成功")
            } else {
                SyncResult.Error("连接失败: HTTP ${response.code}")
            }
        } catch (e: Exception) {
            SyncResult.Error("连接失败: ${e.message}")
        }
    }

    private fun buildObjectKey(remotePath: String): String {
        val prefix = remotePath.trimEnd('/')
        return if (prefix.isNotEmpty()) "$prefix/$BACKUP_KEY" else BACKUP_KEY
    }

    // ========== AWS Signature V4 工具方法 ==========

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return hmacSha256(key, data).joinToString("") { "%02x".format(it) }
    }

    private fun getSignatureKey(key: String, dateStamp: String, region: String, service: String): ByteArray {
        val kDate = hmacSha256("AWS4$key".toByteArray(Charsets.UTF_8), dateStamp)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, AWS4_REQUEST)
    }

    private var currentConfig: S3Config? = null

    fun setConfig(config: S3Config) {
        currentConfig = config
    }

    private fun getCurrentConfig(): S3Config? = currentConfig
}
