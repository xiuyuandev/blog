package com.sushi.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 同步冲突记录 - 来自云端的待处理冲突
 */
@Entity
data class SyncConflict(
    @PrimaryKey val id: String,
    val entityType: String,  // "SKILL" / "TASK" / "RECORD" / etc.
    val entityId: String,
    val localUpdatedAt: Long,
    val remoteUpdatedAt: Long,
    val localData: String,   // JSON 序列化
    val remoteData: String,  // JSON 序列化
    val resolution: String = "PENDING"  // PENDING / LOCAL_WINS / REMOTE_WINS / MERGED
)
