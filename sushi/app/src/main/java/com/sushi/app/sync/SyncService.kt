package com.sushi.app.sync

/**
 * 云同步服务接口
 */
interface SyncService {
    /**
     * 推送数据到云端
     * @param jsonContent 备份 JSON 内容
     * @return 同步结果
     */
    suspend fun push(jsonContent: String): SyncResult

    /**
     * 从云端拉取数据
     * @return JSON 内容或错误
     */
    suspend fun pull(): SyncResult

    /**
     * 测试连接是否正常
     */
    suspend fun testConnection(): SyncResult
}
