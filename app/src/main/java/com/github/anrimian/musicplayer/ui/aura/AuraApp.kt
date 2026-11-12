package com.github.anrimian.musicplayer.ui.aura

import android.content.IntentSender
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.RecoverableSecurityExceptionExt
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry

private class DetailRef(val section: Section, val key: String)

/** A long-pressed song, plus the playlist it was pressed in (if any). */
private class SongMenu(val song: CompositionModel, val entry: PlaylistEntry?, val playlistId: Long?)

@Composable
fun AuraApp(
    controller: AuraController,
    openPlayerSignal: Int,
    onSleepTimer: () -> Unit,
    onEqualizer: () -> Unit,
    onRequestIntent: (IntentSender) -> Unit,
) {
    AuraTheme {
        val songs by controller.songs.asState(emptyList<Composition>())
        val playlists by controller.playlists.asState(emptyList<Playlist>())
        val current by controller.current.asState(CurrentComposition(null, false))
        val currentComposition = current.composition
        val isPlaying = current.isPlaying

        val accent = AuraColors.red

        var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
        val section = Section.entries[sectionIndex]
        var orbOpen by remember { mutableStateOf(false) }
        var playerOpen by rememberSaveable { mutableStateOf(false) }
        var queueOpen by remember { mutableStateOf(false) }
        var focusSearch by remember { mutableStateOf(false) }
        var query by rememberSaveable { mutableStateOf("") }
        var detail by remember { mutableStateOf<DetailRef?>(null) }
        var showSplash by rememberSaveable { mutableStateOf(true) }

        val context = LocalContext.current
        var songMenu by remember { mutableStateOf<SongMenu?>(null) }
        var pickerSongs by remember { mutableStateOf<List<CompositionModel>?>(null) }
        var createPlaylistFor by remember { mutableStateOf<List<CompositionModel>?>(null) }
        var createPlaylistOpen by remember { mutableStateOf(false) }
        var renameSong by remember { mutableStateOf<CompositionModel?>(null) }
        var deleteSong by remember { mutableStateOf<CompositionModel?>(null) }
        var infoSong by remember { mutableStateOf<CompositionModel?>(null) }
        var playlistMenu by remember { mutableStateOf<Playlist?>(null) }
        var renamePlaylist by remember { mutableStateOf<Playlist?>(null) }
        var deletePlaylist by remember { mutableStateOf<Playlist?>(null) }

        fun toast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun onResult(error: Throwable?, done: String?) {
            when {
                error == null -> if (done != null) toast(done)
                error is RecoverableSecurityExceptionExt -> onRequestIntent(error.pIntent.intentSender)
                error.cause is RecoverableSecurityExceptionExt ->
                    onRequestIntent((error.cause as RecoverableSecurityExceptionExt).pIntent.intentSender)
                else -> toast(error.message ?: "Something went wrong")
            }
        }

        LaunchedEffect(openPlayerSignal) {
            if (openPlayerSignal > 0) playerOpen = true
        }

        val q = query.trim()
        val albums = remember(songs) { buildAlbums(songs) }
        val artists = remember(songs) { buildArtists(songs) }
        val filteredSongs = remember(songs, q) {
            if (q.isEmpty()) songs else songs.filter { song ->
                song.title.contains(q, ignoreCase = true)
                    || song.artist.orEmpty().contains(q, ignoreCase = true)
                    || song.album.orEmpty().contains(q, ignoreCase = true)
            }
        }
        val filteredAlbums = remember(albums, q) {
            if (q.isEmpty()) albums else albums.filter {
                it.title.contains(q, true) || it.subtitle.contains(q, true)
            }
        }
        val filteredArtists = remember(artists, q) {
            if (q.isEmpty()) artists else artists.filter { it.title.contains(q, true) }
        }
        val filteredPlaylists = remember(playlists, q) {
            if (q.isEmpty()) playlists else playlists.filter { it.name.contains(q, true) }
        }

        BackHandler(enabled = section != Section.HOME) {
            sectionIndex = Section.HOME.ordinal
            query = ""
        }
        BackHandler(enabled = detail != null) { detail = null }
        BackHandler(enabled = orbOpen) { orbOpen = false }
        BackHandler(enabled = playerOpen) { playerOpen = false }
        BackHandler(enabled = queueOpen) { queueOpen = false }
        BackHandler(enabled = songMenu != null || pickerSongs != null || playlistMenu != null) {
            songMenu = null
            pickerSongs = null
            playlistMenu = null
        }

        val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val listTop = top + 12.dp + HEADER_HEIGHT + 12.dp
        val listBottom = bottom + 124.dp

        Box(
            Modifier
                .fillMaxSize()
                .background(AuraColors.bg)
        ) {
            AuraBackdrop()

            AnimatedContent(
                targetState = detail,
                transitionSpec = {
                    if (targetState != null) {
                        (fadeIn(tween(320)) + slideInHorizontally(tween(320)) { it / 5 }) togetherWith
                            fadeOut(tween(200))
                    } else {
                        fadeIn(tween(280)) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally(tween(280)) { it / 5 })
                    }
                },
                label = "detail"
            ) { ref ->
                if (ref == null) {
                    HomeContent(
                        section = section,
                        songs = filteredSongs,
                        allSongs = songs,
                        albums = filteredAlbums,
                        artists = filteredArtists,
                        playlists = filteredPlaylists,
                        albumCount = albums.size,
                        artistCount = artists.size,
                        playlistCount = playlists.size,
                        current = currentComposition,
                        isPlaying = isPlaying,
                        accent = accent,
                        query = query,
                        focusSearch = focusSearch,
                        listTop = listTop,
                        headerTop = top + 12.dp,
                        listBottom = listBottom,
                        controller = controller,
                        onQuery = { query = it },
                        onFocusHandled = { focusSearch = false },
                        onSearchFromHome = {
                            focusSearch = true
                            query = ""
                            sectionIndex = Section.SONGS.ordinal
                        },
                        onOpenSection = { target ->
                            query = ""
                            sectionIndex = target.ordinal
                        },
                        onOpenPlayer = { playerOpen = true },
                        onSongMenu = { song -> songMenu = SongMenu(song, null, null) },
                        onPlaylistMenu = { playlist -> playlistMenu = playlist },
                        onNewPlaylist = {
                            createPlaylistFor = null
                            createPlaylistOpen = true
                        },
                        onOpenGroup = { group -> detail = DetailRef(section, group.key) },
                        onOpenPlaylist = { playlist -> detail = DetailRef(Section.PLAYLISTS, playlist.id.toString()) }
                    )
                } else {
                    DetailContent(
                        ref = ref,
                        albums = albums,
                        artists = artists,
                        playlists = playlists,
                        currentId = currentComposition?.id,
                        isPlaying = isPlaying,
                        accent = accent,
                        top = listTop,
                        headerTop = top + 12.dp,
                        bottom = listBottom,
                        controller = controller,
                        onSongMenu = { menu -> songMenu = menu },
                        onBack = { detail = null }
                    )
                }
            }

            // scrim behind the opened orb
            AnimatedVisibility(
                visible = orbOpen,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { orbOpen = false }
                )
            }

            // dock: orb floats above a full-width mini player
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = bottom + 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                OrbNav(
                    open = orbOpen,
                    section = section,
                    accent = accent,
                    onToggle = { orbOpen = !orbOpen }
                )
                AnimatedVisibility(
                    visible = currentComposition != null,
                    enter = fadeIn() + slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    if (currentComposition != null) {
                        MiniPlayer(
                            controller = controller,
                            composition = currentComposition,
                            isPlaying = isPlaying,
                            accent = accent,
                            onOpen = { playerOpen = true },
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }

            OrbFan(
                open = orbOpen,
                section = section,
                accent = accent,
                originBottom = bottom + 16.dp + (if (currentComposition != null) 84.dp else 0.dp),
                onSelect = { picked ->
                    sectionIndex = picked.ordinal
                    query = ""
                    detail = null
                    orbOpen = false
                }
            )

            // full player
            PlayerReveal(
                visible = playerOpen && currentComposition != null,
                bottomInset = bottom
            ) {
                if (currentComposition != null) {
                    Box(Modifier.fillMaxSize()) {
                        PlayerScreen(
                            controller = controller,
                            composition = currentComposition,
                            isPlaying = isPlaying,
                            accent = accent,
                            onClose = { playerOpen = false },
                            onQueue = { queueOpen = true },
                            onSleepTimer = onSleepTimer,
                            onEqualizer = onEqualizer,
                            onAddToPlaylist = { pickerSongs = listOf(currentComposition) }
                        )
                        AnimatedVisibility(
                            visible = queueOpen,
                            enter = slideInVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)) { it },
                            exit = slideOutVertically(tween(260)) { it }
                        ) {
                            QueueSheet(
                                controller = controller,
                                isPlaying = isPlaying,
                                accent = accent,
                                onClose = { queueOpen = false }
                            )
                        }
                    }
                }
            }

            // ---- long-press menus, pickers and dialogs (always on top) ----
            val menu = songMenu
            if (menu != null) {
                val song = menu.song
                val items = ArrayList<ActionItem>()
                items.add(ActionItem(AuraIcons.PlayNext, "Play next") {
                    controller.playNext(listOf(song)) { onResult(it, "Will play next") }
                })
                items.add(ActionItem(AuraIcons.Queue, "Add to queue") {
                    controller.addToQueue(listOf(song)) { onResult(it, "Added to queue") }
                })
                items.add(ActionItem(AuraIcons.AddToPlaylist, "Add to playlist") { pickerSongs = listOf(song) })
                if (menu.entry != null && menu.playlistId != null) {
                    items.add(ActionItem(AuraIcons.Close, "Remove from playlist") {
                        controller.removeFromPlaylist(menu.entry, menu.playlistId)
                    })
                }
                items.add(ActionItem(AuraIcons.Rename, "Rename") { renameSong = song })
                items.add(ActionItem(AuraIcons.Info, "Details") { infoSong = song })
                items.add(ActionItem(AuraIcons.Delete, "Delete from device", danger = true) { deleteSong = song })
                ActionSheet(
                    title = song.title,
                    subtitle = song.artist.orEmpty().ifBlank { "Unknown artist" },
                    cover = song,
                    accent = accent,
                    items = items,
                    onDismiss = { songMenu = null }
                )
            }

            val picking = pickerSongs
            if (picking != null) {
                PlaylistPickerSheet(
                    playlists = playlists,
                    accent = accent,
                    onPick = { playlist ->
                        controller.addToPlaylist(picking, playlist) { onResult(it, "Added to " + playlist.name) }
                    },
                    onCreate = {
                        createPlaylistFor = picking
                        createPlaylistOpen = true
                    },
                    onDismiss = { pickerSongs = null }
                )
            }

            val pMenu = playlistMenu
            if (pMenu != null) {
                ActionSheet(
                    title = pMenu.name,
                    subtitle = pMenu.compositionsCount.toString() + " songs",
                    cover = null,
                    accent = accent,
                    items = listOf(
                        ActionItem(AuraIcons.Play, "Play") { controller.playPlaylist(pMenu) },
                        ActionItem(AuraIcons.Rename, "Rename") { renamePlaylist = pMenu },
                        ActionItem(AuraIcons.Delete, "Delete playlist", danger = true) { deletePlaylist = pMenu }
                    ),
                    onDismiss = { playlistMenu = null }
                )
            }

            if (createPlaylistOpen) {
                TextInputDialog(
                    title = "New playlist",
                    label = "Name",
                    initial = "",
                    confirmLabel = "Create",
                    accent = accent,
                    onConfirm = { name ->
                        val pending = createPlaylistFor
                        controller.createPlaylist(name) { created, error ->
                            if (created != null && pending != null) {
                                controller.addToPlaylist(pending, created) { onResult(it, "Added to " + created.name) }
                            } else {
                                onResult(error, "Playlist created")
                            }
                        }
                    },
                    onDismiss = {
                        createPlaylistOpen = false
                        createPlaylistFor = null
                    }
                )
            }

            val renaming = renameSong
            if (renaming != null) {
                RenameSongDialog(
                    song = renaming,
                    controller = controller,
                    accent = accent,
                    onResult = { onResult(it, "Renamed") },
                    onDismiss = { renameSong = null }
                )
            }

            val deleting = deleteSong
            if (deleting != null) {
                ConfirmDialog(
                    title = "Delete song?",
                    message = "\"" + deleting.title + "\" will be permanently deleted from your device. " +
                        "This also removes it from the queue and playlists.",
                    confirmLabel = "Delete",
                    danger = true,
                    accent = accent,
                    onConfirm = { controller.deleteSongs(listOf(deleting)) { onResult(it, "Deleted") } },
                    onDismiss = { deleteSong = null }
                )
            }

            val info = infoSong
            if (info != null) {
                SongInfoDialog(info, accent) { infoSong = null }
            }

            val renamingPlaylist = renamePlaylist
            if (renamingPlaylist != null) {
                TextInputDialog(
                    title = "Rename playlist",
                    label = "Name",
                    initial = renamingPlaylist.name,
                    confirmLabel = "Save",
                    accent = accent,
                    onConfirm = { controller.renamePlaylist(renamingPlaylist, it) },
                    onDismiss = { renamePlaylist = null }
                )
            }

            val deletingPlaylist = deletePlaylist
            if (deletingPlaylist != null) {
                ConfirmDialog(
                    title = "Delete playlist?",
                    message = "\"" + deletingPlaylist.name + "\" will be deleted. Your songs stay on the device.",
                    confirmLabel = "Delete",
                    danger = true,
                    accent = accent,
                    onConfirm = { controller.deletePlaylist(deletingPlaylist) },
                    onDismiss = { deletePlaylist = null }
                )
            }

            if (showSplash) {
                AuraSplash(onFinished = { showSplash = false })
            }
        }
    }
}

@Composable
private fun HomeContent(
    section: Section,
    songs: List<Composition>,
    allSongs: List<Composition>,
    albums: List<TrackGroup>,
    artists: List<TrackGroup>,
    playlists: List<Playlist>,
    albumCount: Int,
    artistCount: Int,
    playlistCount: Int,
    current: CompositionModel?,
    isPlaying: Boolean,
    accent: Color,
    query: String,
    focusSearch: Boolean,
    listTop: androidx.compose.ui.unit.Dp,
    headerTop: androidx.compose.ui.unit.Dp,
    listBottom: androidx.compose.ui.unit.Dp,
    controller: AuraController,
    onQuery: (String) -> Unit,
    onFocusHandled: () -> Unit,
    onSearchFromHome: () -> Unit,
    onOpenSection: (Section) -> Unit,
    onOpenPlayer: () -> Unit,
    onSongMenu: (CompositionModel) -> Unit,
    onPlaylistMenu: (Playlist) -> Unit,
    onNewPlaylist: () -> Unit,
    onOpenGroup: (TrackGroup) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
) {
    val currentId = current?.id
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 5 -> "Late night"
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Box(Modifier.fillMaxSize()) {
    AnimatedContent(
        targetState = section,
        transitionSpec = {
            (fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 14 }) togetherWith fadeOut(tween(160))
        },
        label = "section"
    ) { target ->
        // the title/search live in the floating header; only the extra buttons scroll with the list
        val header: @Composable () -> Unit = {
            Column {
                if (target == Section.SONGS && query.isEmpty()) {
                    SongsHero(
                        accent = accent,
                        onPlayAll = { controller.play(allSongs, 0) },
                        onShuffle = { controller.shuffleAll(allSongs) }
                    )
                }
                if (target == Section.PLAYLISTS) {
                    PillButton("New playlist", AuraIcons.Add, accent, Color.Black.copy(alpha = 0.85f), onNewPlaylist)
                }
            }
        }
        when (target) {
            Section.HOME -> HomeScreen(
                songs = allSongs,
                albumCount = albumCount,
                artistCount = artistCount,
                playlistCount = playlistCount,
                current = current,
                isPlaying = isPlaying,
                accent = accent,
                topPadding = listTop,
                bottomPadding = listBottom,
                controller = controller,
                onOpenSection = onOpenSection,
                onOpenPlayer = onOpenPlayer
            )

            Section.SONGS -> if (songs.isEmpty()) {
                EmptyScreen(listTop, if (query.isEmpty()) "No songs found yet" else "Nothing matches your search", header)
            } else {
                TrackList(
                    tracks = songs,
                    currentId = currentId,
                    playing = isPlaying,
                    accent = accent,
                    topPadding = listTop,
                    bottomPadding = listBottom,
                    onPlay = { index -> controller.play(songs, index) },
                    onLongPress = { index -> onSongMenu(songs[index]) },
                    header = header
                )
            }

            Section.ALBUMS -> if (albums.isEmpty()) {
                EmptyScreen(listTop, "No albums", header)
            } else {
                AlbumGrid(albums, listTop, listBottom, onOpenGroup, header)
            }

            Section.ARTISTS -> if (artists.isEmpty()) {
                EmptyScreen(listTop, "No artists", header)
            } else {
                ArtistList(artists, listTop, listBottom, onOpenGroup, header)
            }

            Section.PLAYLISTS -> if (playlists.isEmpty()) {
                EmptyScreen(listTop, "No playlists yet", header)
            } else {
                PlaylistList(playlists, listTop, listBottom, onOpenPlaylist, onPlaylistMenu, header)
            }
        }
    }
    val subtitle = when (section) {
        Section.HOME -> "What's the mood today?"
        Section.SONGS -> allSongs.size.toString() + " songs"
        Section.ALBUMS -> albumCount.toString() + " albums"
        Section.ARTISTS -> artistCount.toString() + " artists"
        Section.PLAYLISTS -> playlistCount.toString() + " playlists"
    }
    FloatingHeader(
        icon = section.icon,
        title = if (section == Section.HOME) greeting else section.title,
        subtitle = subtitle,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = headerTop),
        onSearch = if (section == Section.HOME) onSearchFromHome else null,
        query = query,
        onQuery = if (section == Section.HOME) null else onQuery,
        placeholder = "Search " + section.title.lowercase(),
        focusRequest = focusSearch && section == Section.SONGS,
        onFocusHandled = onFocusHandled,
        resetKey = section
    )
    }
}

@Composable
private fun DetailContent(
    ref: DetailRef,
    albums: List<TrackGroup>,
    artists: List<TrackGroup>,
    playlists: List<Playlist>,
    currentId: Long?,
    isPlaying: Boolean,
    accent: Color,
    top: androidx.compose.ui.unit.Dp,
    headerTop: androidx.compose.ui.unit.Dp,
    bottom: androidx.compose.ui.unit.Dp,
    controller: AuraController,
    onSongMenu: (SongMenu) -> Unit,
    onBack: () -> Unit,
) {
    var title = ""
    var subtitle = ""
    var tracks: List<CompositionModel> = emptyList()

    when (ref.section) {
        Section.ALBUMS, Section.ARTISTS -> {
            val source = if (ref.section == Section.ALBUMS) albums else artists
            val group = source.firstOrNull { it.key == ref.key }
            if (group != null) {
                title = group.title
                subtitle = group.tracks.size.toString() + " songs" +
                    if (ref.section == Section.ALBUMS) " · " + group.subtitle else ""
                tracks = group.tracks
            }
        }

        else -> {
            val id = ref.key.toLongOrNull() ?: -1L
            val playlist = playlists.firstOrNull { it.id == id }
            val entries by remember(id) { controller.playlistEntries(id) }.asState(emptyList())
            title = playlist?.name.orEmpty()
            subtitle = entries.size.toString() + " songs"
            tracks = entries
        }
    }

    Box(Modifier.fillMaxSize()) {
        TrackList(
            tracks = tracks,
            currentId = currentId,
            playing = isPlaying,
            accent = accent,
            topPadding = top,
            bottomPadding = bottom,
            onPlay = { index -> controller.play(tracks, index) },
            onLongPress = { index ->
                val item = tracks[index]
                val playlistId = ref.key.toLongOrNull()
                onSongMenu(
                    if (item is PlaylistEntry && ref.section == Section.PLAYLISTS) SongMenu(item, item, playlistId)
                    else SongMenu(item, null, null)
                )
            },
            header = {
                DetailHeader(
                    title = title,
                    subtitle = subtitle,
                    cover = tracks.firstOrNull(),
                    accent = accent,
                    onPlay = { controller.play(tracks, 0) },
                    onShuffle = { controller.shuffleAll(tracks) }
                )
            }
        )
        FloatingHeader(
            icon = ref.section.icon,
            title = title,
            subtitle = subtitle,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = headerTop),
            onBack = onBack
        )
    }
}
