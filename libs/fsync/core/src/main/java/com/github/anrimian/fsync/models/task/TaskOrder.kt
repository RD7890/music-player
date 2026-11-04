package com.github.anrimian.fsync.models.task

object TaskOrder {
    const val DELETE_REMOTE = 1
    const val MOVE_REMOTE = 2
    const val MOVE_LOCAL = 3
    const val DOWNLOAD = 4
    const val UPLOAD = 5
    const val DELETE_LOCAL = 6
}