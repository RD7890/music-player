package com.github.anrimian.fsync.models.state.file

import com.github.anrimian.fsync.models.ProgressInfo
import com.github.anrimian.fsync.models.storage.RemoteStorageInfo

class FileSyncState(
    val taskType: FileTaskType,
    val storageInfo: RemoteStorageInfo
) {

    private var progressInfo = ProgressInfo()

    fun setProgress(progressInfo: ProgressInfo): FileSyncState {
        this.progressInfo = progressInfo
        return this
    }

    fun getProgress() = progressInfo

    override fun toString(): String {
        return "Downloading(storageInfo=$storageInfo, progressInfo=$progressInfo)"
    }

}
