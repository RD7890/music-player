package com.github.anrimian.fsync.models.storage

interface RemoteStorageCredentials {
    fun getStorageType(): Int
}