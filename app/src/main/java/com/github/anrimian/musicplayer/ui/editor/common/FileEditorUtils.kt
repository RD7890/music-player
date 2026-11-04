package com.github.anrimian.musicplayer.ui.editor.common

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.fsync.models.ProgressInfo
import com.github.anrimian.fsync.models.state.file.FileTaskType
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.utils.rx.doOnFirst
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

fun performFilesChangeAction(
    syncInteractor: SyncInteractor<FileKey, *, Long>,
    uiScheduler: Scheduler,
    onFilesPrepared: (Int) -> Unit,
    onFileDownloading: (ProgressInfo) -> Unit,
    onFilesEdited: (Int) -> Unit,
    action: (
        downloadingSubject: BehaviorSubject<Long>,
        editingSubject: BehaviorSubject<Long>
    ) -> Completable
): Completable {
    return Single.create { emitter ->
        val downloadingSubject = BehaviorSubject.create<Long>()
        val editingSubject = BehaviorSubject.create<Long>()
        var preparedFilesCount = 0
        var editedFileCount = 0

        val disposable = CompositeDisposable()
        disposable.add(
            downloadingSubject
                .ignoreElements()
                .andThen(editingSubject)
                .observeOn(uiScheduler)
                .subscribe { onFilesEdited(++editedFileCount) }
        )
        disposable.add(
            downloadingSubject
                .switchMap { id ->
                    syncInteractor.getFileSyncStateObservable(id)
                        .observeOn(uiScheduler)
                        .filter { stateOpt -> stateOpt.value?.taskType == FileTaskType.DOWNLOAD }
                        .doOnFirst { onFilesPrepared(++preparedFilesCount) }
                }
                .subscribe { stateOpt ->
                    val state = stateOpt.value
                    if (state != null) {
                        onFileDownloading(state.getProgress())
                    }
                }
        )
        val completable = action(downloadingSubject, editingSubject)
            .doFinally { disposable.dispose() }
        emitter.onSuccess(completable)
    }.flatMapCompletable { completable -> completable }
        .observeOn(uiScheduler)
}