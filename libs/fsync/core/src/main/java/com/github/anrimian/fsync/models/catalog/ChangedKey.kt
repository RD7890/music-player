package com.github.anrimian.fsync.models.catalog

data class ChangedKey<K>(val oldKey: K, val newKey: K, val lastKeyModifyTime: Long?)