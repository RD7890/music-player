package com.github.anrimian.fsync.models.storage

data class DisableState(
    val disableReason: DisableReason,
    val message: String?
)