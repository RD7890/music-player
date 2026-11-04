package com.github.anrimian.musicplayer.ui.aura

import com.github.anrimian.musicplayer.di.app.AppComponent
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry
import io.reactivex.rxjava3.core.Observable

/** Thin bridge between the existing domain layer (playback engine, library) and the Aura UI. */
class AuraController(component: AppComponent) {

    val player = component.libraryPlayerInteractor()
    private val library = component.libraryCompositionsInteractor()
    private val playlistsInteractor = component.playlistsInteractor()

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
}

enum class Section(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists");

    val icon get() = when (this) {
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
