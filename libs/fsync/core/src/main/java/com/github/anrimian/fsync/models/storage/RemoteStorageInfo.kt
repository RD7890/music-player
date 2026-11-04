package com.github.anrimian.fsync.models.storage

class RemoteStorageInfo(
    val id: Long,
    val storageType: Int,
    val localRootPath: String,
    val remoteRootPath: String,
    val accountInfo: StorageAccountInfo
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteStorageInfo

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "RemoteStorageInfo(id=$id, storageType=$storageType)"
    }


}