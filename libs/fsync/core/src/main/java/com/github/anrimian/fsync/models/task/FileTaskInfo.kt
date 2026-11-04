package com.github.anrimian.fsync.models.task

import com.github.anrimian.fsync.models.storage.RemoteStorageInfo

class FileTaskInfo<T>(
    val id: Long,
    val taskOrder: Int,
    val taskCreateTime: Long,
    val createReason: Int,
    val createReasonDescription: String?,
    val taskCompleteTime: Long?,
    val blockReason: BlockReason?,
    val storageInfo: RemoteStorageInfo,
    val fileKey: T?,
    val fileKeyFrom: T?,
    val fileKeyTo: T?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileTaskInfo<*>) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}