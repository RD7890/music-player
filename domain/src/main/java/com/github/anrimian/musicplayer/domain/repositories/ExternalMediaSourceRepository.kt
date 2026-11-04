package com.github.anrimian.musicplayer.domain.repositories

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import io.reactivex.rxjava3.core.Single

interface ExternalMediaSourceRepository {

    fun getCompositionSource(fileRef: FileReference): Single<CompositionSource>

    fun deleteAllData()

}