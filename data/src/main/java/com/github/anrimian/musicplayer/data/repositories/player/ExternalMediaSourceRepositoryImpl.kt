package com.github.anrimian.musicplayer.data.repositories.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.repositories.ExternalMediaSourceRepository
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExternalMediaSourceRepositoryImpl(
    private val context: Context,
    private val ioScheduler: Scheduler,
    private val cache: ExternalAudioFileCache,
) : ExternalMediaSourceRepository {

    override fun getCompositionSource(fileRef: FileReference): Single<CompositionSource> {
        if (fileRef !is UriFileReference) {
            throw IllegalArgumentException()
        }
        val originalUri = fileRef.uri
        return cache.getAudioFile(originalUri)
            .map { cachedFile ->
                ExternalCompositionSourceBuilder(cachedFile.uri)
                    .displayName(cachedFile.displayName)
                    .size(cachedFile.size)
            }
            .switchIfEmpty(
                Single.defer {
                    Single.just(ExternalCompositionSourceBuilder(originalUri))
                        .map(::readDataFromContentResolver)
                        .timeout(2, TimeUnit.SECONDS)
                        .doOnSuccess { builder ->
                            cache.runAudioFileSaving(originalUri, builder.displayName)
                        }
                }
            )
            .flatMap { builder ->
                Single.fromCallable { readDataFromFile(builder) }
                    .timeout(2, TimeUnit.SECONDS)
                    .subscribeOn(ioScheduler)
                    .onErrorReturnItem(builder)
                    .map(ExternalCompositionSourceBuilder::build)
            }
    }

    override fun deleteAllData() {
        cache.clearCache()
    }

    private fun readDataFromContentResolver(
        builder: ExternalCompositionSourceBuilder,
    ): ExternalCompositionSourceBuilder {
        var displayName: String? = null
        var size: Long = 0
        try {
            context.contentResolver.query(
                builder.uri,
                arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE),
                null,
                null,
                null
            ).use { cursor ->
                val cursorWrapper = CursorWrapper(cursor)
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME)
                    size = cursorWrapper.getLong(MediaStore.Audio.Media.SIZE)
                }
            }
        } catch (ex: SecurityException) {
            // if we got here - it means that we don't have cache file
            //  and there's no sense trying to read further
            throw ex
        } catch (ignored: Exception) {}
        return builder.displayName(displayName ?: builder.uri.lastPathSegment ?: "unknown name")
            .size(size)
    }

    private fun readDataFromFile(
        builder: ExternalCompositionSourceBuilder,
    ): ExternalCompositionSourceBuilder {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration: Long = 0
        var mmr: MediaMetadataRetriever? = null
        try {
            mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, builder.uri)
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            try {
                duration = durationStr!!.toLong()
            } catch (ignored: NumberFormatException) {}
        } catch (ignored: Exception) {
        } finally {
            if (mmr != null) {
                try {
                    mmr.release()
                } catch (ignored: IOException) {}
            }
        }
        return builder.title(title)
            .artist(artist)
            .album(album)
            .duration(duration)
    }

    class ExternalCompositionSourceBuilder(val uri: Uri) {
        lateinit var displayName: String
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration: Long = 0L
        var size: Long = 0L

        fun displayName(displayName: String) = apply { this.displayName = displayName }

        fun title(title: String?) = apply { this.title = title }

        fun artist(artist: String?) = apply { this.artist = artist }

        fun album(album: String?) = apply { this.album = album }

        fun duration(duration: Long) = apply { this.duration = duration }

        fun size(size: Long) = apply { this.size = size }

        fun build() = ExternalCompositionSource(
            uri = uri,
            displayName = displayName,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            size = size,
        )
    }

}