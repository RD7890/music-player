package com.github.anrimian.fsync.models.storage

class RemoteStorageFullInfo(
    val storageInfo: RemoteStorageInfo,
    val spaceUsage: StorageSpaceUsage,
    val disableState: DisableState?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemoteStorageFullInfo

        if (storageInfo != other.storageInfo) return false

        return true
    }

    override fun hashCode(): Int {
        return storageInfo.hashCode()
    }
}