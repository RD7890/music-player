package com.github.anrimian.fsync.models.state.file

import com.github.anrimian.fsync.models.ProgressInfo
import com.github.anrimian.fsync.models.storage.RemoteStorageInfo
import io.reactivex.rxjava3.core.Observable

class FileTaskState(
    val taskType: FileTaskType,
    val storageInfo: RemoteStorageInfo,
    val progressObservable: Observable<ProgressInfo>
) {
    override fun toString(): String {
        return "TaskState($taskType)"
    }
}
