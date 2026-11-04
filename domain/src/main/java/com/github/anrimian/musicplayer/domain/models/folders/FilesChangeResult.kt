package com.github.anrimian.musicplayer.domain.models.folders

import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.sync.FileKey

class FilesChangeResult(
    val changedFiles: List<ChangedCompositionPath>,
    val restoredFiles: List<FileKey>,
    val changeTime: Long
)