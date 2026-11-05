package com.github.anrimian.musicplayer.ui.aura

import com.github.anrimian.musicplayer.di.app.AppComponent
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Thin bridge between the existing domain layer (playback engine, library) and the Aura UI. */
class AuraController(component: AppComponent) {

    val player = component.libraryPlayerInteractor()
    private val library = component.libraryCompositionsInteractor()
    private val playlistsInteractor = component.playlistsInteractor()
    private val editor = component.auraEditorInteractor()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val songs: Observable<List<Composition>> = library.getCompositionsObservable(null)
    val playlists: Observable<List<Playlist>> = playlistsInteractor.getPlaylistsObservable()
    val current: Observable<CurrentComposition> = player.getCurrentCompositionObservable()
    val position: Observable<Long> = player.getTrackPositionObservable()
    val repeatMode: Observable<Int> = player.getRepeatModeObservable()
    val shuffle: Observable<Boolean> = player.getRandomPlayingObservable()
    val queue: Observable<List<PlayQueueItem>> = player.getPlayQueueObservable().toObservable()
    val queueEvent: Observable<PlayQueueEvent> = player.getCurrentQueueItemObservable()
    val speed: Observable<Float> = player.getPlaybackSpeedObservable()

    fun playlistEntries(id: Long): Observable<List<PlaylistEntry>> {
        return playlistsInteractor.getCompositionsObservable(id, null)
    }

    fun play(list: List<CompositionModel>, index: Int) {
        player.setCompositionsQueueAndPlay(list, index).onErrorComplete().subscribe()
    }

    fun shuffleAll(list: List<CompositionModel>) {
        if (list.isEmpty()) return
        player.setRandomPlayingEnabled(true)
        player.setCompositionsQueueAndPlay(list).onErrorComplete().subscribe()
    }

    // ---- queue ----

    fun playNext(items: List<CompositionModel>, onResult: (Throwable?) -> Unit) {
        run(player.addCompositionsToPlayNext(items).ignoreElement(), onResult)
    }

    fun addToQueue(items: List<CompositionModel>, onResult: (Throwable?) -> Unit) {
        run(player.addCompositionsToEnd(items).ignoreElement(), onResult)
    }

    fun removeFromQueue(item: PlayQueueItem) {
        player.removeQueueItem(item).onErrorComplete().subscribe()
    }

    fun clearQueue() {
        player.clearPlayQueue().onErrorComplete().subscribe()
    }

    /** Moves an item by a chain of neighbour swaps, the only reorder primitive the engine has. */
    fun moveQueueItem(snapshot: List<PlayQueueItem>, from: Int, to: Int) {
        if (from == to || from !in snapshot.indices || to !in snapshot.indices) return
        val working = snapshot.toMutableList()
        val step = if (to > from) 1 else -1
        var chain = Completable.complete()
        var cursor = from
        while (cursor != to) {
            val a = working[cursor]
            val b = working[cursor + step]
            chain = chain.andThen(player.swapItems(a, b))
            working[cursor] = b
            working[cursor + step] = a
            cursor += step
        }
        chain.onErrorComplete().subscribe()
    }

    // ---- playlists ----

    fun createPlaylist(name: String, onResult: (Playlist?, Throwable?) -> Unit) {
        playlistsInteractor.createPlaylist(name)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onResult(it, null) }, { onResult(null, it) })
    }

    fun addToPlaylist(items: List<CompositionModel>, playlist: Playlist, onResult: (Throwable?) -> Unit) {
        run(
            playlistsInteractor.addCompositionsToPlaylist(items, playlist, false, false).ignoreElement(),
            onResult
        )
    }

    fun removeFromPlaylist(entry: PlaylistEntry, playlistId: Long) {
        playlistsInteractor.deleteItemFromPlaylist(entry, playlistId).onErrorComplete().subscribe()
    }

    fun renamePlaylist(playlist: Playlist, name: String) {
        scope.launch {
            try {
                playlistsInteractor.updatePlaylistName(playlist.id, name)
            } catch (ignored: Exception) {
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        scope.launch {
            try {
                playlistsInteractor.deletePlaylist(playlist.id)
            } catch (ignored: Exception) {
            }
        }
    }

    fun playPlaylist(playlist: Playlist) {
        scope.launch {
            try {
                playlistsInteractor.startPlaying(listOf(playlist))
            } catch (ignored: Exception) {
            }
        }
    }

    // ---- editing / deleting songs ----

    fun loadFullComposition(id: Long, onResult: (FullComposition?) -> Unit) {
        editor.getCompositionObservable(id)
            .firstOrError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onResult(it) }, { onResult(null) })
    }

    fun rename(id: Long, newTitle: String?, newFileName: String?, onResult: (Throwable?) -> Unit) {
        var chain = Completable.complete()
        if (newTitle != null) chain = chain.andThen(editor.editCompositionTitle(id, newTitle))
        if (newFileName != null) chain = chain.andThen(editor.editCompositionFileName(id, newFileName))
        run(chain, onResult)
    }

    fun deleteSongs(items: List<CompositionModel>, onResult: (Throwable?) -> Unit) {
        run(player.deleteCompositions(items).ignoreElement(), onResult)
    }

    private fun run(completable: Completable, onResult: (Throwable?) -> Unit) {
        completable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onResult(null) }, { onResult(it) })
    }
}

enum class Section(val title: String) {
    HOME("Home"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists");

    val icon get() = when (this) {
        HOME -> AuraIcons.Home
        SONGS -> AuraIcons.Songs
        ALBUMS -> AuraIcons.Albums
        ARTISTS -> AuraIcons.Artists
        PLAYLISTS -> AuraIcons.Playlists
    }
}

/** Albums and artists are derived from the song list, so they always match the library. */
class TrackGroup(
    val key: String,
    val title: String,
    val subtitle: String,
    val tracks: List<Composition>,
)

fun buildAlbums(songs: List<Composition>): List<TrackGroup> {
    return songs
        .groupBy { song -> song.album.orEmpty().ifBlank { "Unknown album" } + "\u0000" + song.artist.orEmpty() }
        .map { (key, tracks) ->
            val first = tracks.first()
            TrackGroup(
                key = key,
                title = first.album.orEmpty().ifBlank { "Unknown album" },
                subtitle = first.artist.orEmpty().ifBlank { "Unknown artist" },
                tracks = tracks
            )
        }
        .sortedBy { it.title.lowercase() }
}

fun buildArtists(songs: List<Composition>): List<TrackGroup> {
    return songs
        .groupBy { song -> song.artist.orEmpty().ifBlank { "Unknown artist" } }
        .map { (key, tracks) ->
            TrackGroup(
                key = key,
                title = key,
                subtitle = tracks.size.toString() + if (tracks.size == 1) " song" else " songs",
                tracks = tracks
            )
        }
        .sortedBy { it.title.lowercase() }
}
