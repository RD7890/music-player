package com.github.anrimian.musicplayer.ui.aura

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist

private class DetailRef(val section: Section, val key: String)

@Composable
fun AuraApp(
    controller: AuraController,
    openPlayerSignal: Int,
    onSleepTimer: () -> Unit,
    onEqualizer: () -> Unit,
) {
    AuraTheme {
        val songs by controller.songs.asState(emptyList<Composition>())
        val playlists by controller.playlists.asState(emptyList<Playlist>())
        val current by controller.current.asState(CurrentComposition(null, false))
        val currentComposition = current.composition
        val isPlaying = current.isPlaying

        val accent by animateColorAsState(rememberAccent(currentComposition), tween(700), label = "accent")

        var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
        val section = Section.entries[sectionIndex]
        var orbOpen by remember { mutableStateOf(false) }
        var playerOpen by rememberSaveable { mutableStateOf(false) }
        var queueOpen by remember { mutableStateOf(false) }
        var searchOpen by rememberSaveable { mutableStateOf(false) }
        var query by rememberSaveable { mutableStateOf("") }
        var detail by remember { mutableStateOf<DetailRef?>(null) }

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

        BackHandler(enabled = detail != null) { detail = null }
        BackHandler(enabled = orbOpen) { orbOpen = false }
        BackHandler(enabled = playerOpen) { playerOpen = false }
        BackHandler(enabled = queueOpen) { queueOpen = false }

        val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val listTop = top + 128.dp + if (searchOpen) 66.dp else 0.dp
        val listBottom = bottom + 124.dp

        Box(
            Modifier
                .fillMaxSize()
                .background(AuraColors.bg)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to accent.copy(alpha = 0.34f),
                            0.5f to AuraColors.bg,
                            1f to AuraColors.bg
                        )
                    )
            )

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
                        albums = filteredAlbums,
                        artists = filteredArtists,
                        playlists = filteredPlaylists,
                        totalSongs = songs.size,
                        currentId = currentComposition?.id,
                        isPlaying = isPlaying,
                        accent = accent,
                        searchOpen = searchOpen,
                        query = query,
                        top = top,
                        listTop = listTop,
                        listBottom = listBottom,
                        controller = controller,
                        onQuery = { query = it },
                        onToggleSearch = {
                            searchOpen = !searchOpen
                            if (!searchOpen) query = ""
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
                        top = top,
                        bottom = listBottom,
                        controller = controller,
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

            // dock: mini player pill + orb
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = bottom + 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    AnimatedVisibility(
                        visible = currentComposition != null && !orbOpen,
                        enter = fadeIn() + slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it },
                        exit = fadeOut() + slideOutVertically { it }
                    ) {
                        if (currentComposition != null) {
                            MiniPlayer(
                                controller = controller,
                                composition = currentComposition,
                                isPlaying = isPlaying,
                                accent = accent,
                                onOpen = { playerOpen = true }
                            )
                        }
                    }
                }
                Box(Modifier.padding(start = 12.dp)) {
                    OrbNav(
                        open = orbOpen,
                        section = section,
                        accent = accent,
                        onToggle = { orbOpen = !orbOpen },
                        onSelect = { picked ->
                            sectionIndex = picked.ordinal
                            detail = null
                            orbOpen = false
                        }
                    )
                }
            }

            // full player
            AnimatedVisibility(
                visible = playerOpen && currentComposition != null,
                enter = slideInVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(),
                exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(220))
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
                            onEqualizer = onEqualizer
                        )
                        AnimatedVisibility(
                            visible = queueOpen,
                            enter = slideInVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)) { it },
                            exit = slideOutVertically(tween(260)) { it }
                        ) {
                            QueueSheet(
                                controller = controller,
                                currentComposition = currentComposition,
                                isPlaying = isPlaying,
                                accent = accent,
                                onClose = { queueOpen = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    section: Section,
    songs: List<Composition>,
    albums: List<TrackGroup>,
    artists: List<TrackGroup>,
    playlists: List<Playlist>,
    totalSongs: Int,
    currentId: Long?,
    isPlaying: Boolean,
    accent: Color,
    searchOpen: Boolean,
    query: String,
    top: androidx.compose.ui.unit.Dp,
    listTop: androidx.compose.ui.unit.Dp,
    listBottom: androidx.compose.ui.unit.Dp,
    controller: AuraController,
    onQuery: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onOpenGroup: (TrackGroup) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
) {
    val subtitle = when (section) {
        Section.SONGS -> totalSongs.toString() + " songs in your library"
        Section.ALBUMS -> albums.size.toString() + " albums"
        Section.ARTISTS -> artists.size.toString() + " artists"
        Section.PLAYLISTS -> playlists.size.toString() + " playlists"
    }
    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = section,
            transitionSpec = {
                (fadeIn(tween(320)) + slideInVertically(tween(320)) { it / 14 }) togetherWith fadeOut(tween(160))
            },
            label = "section"
        ) { target ->
            when (target) {
                Section.SONGS -> if (songs.isEmpty()) {
                    EmptyHint(if (query.isEmpty()) "No songs found yet" else "Nothing matches your search")
                } else {
                    TrackList(
                        tracks = songs,
                        currentId = currentId,
                        playing = isPlaying,
                        accent = accent,
                        topPadding = listTop,
                        bottomPadding = listBottom,
                        onPlay = { index -> controller.play(songs, index) }
                    )
                }

                Section.ALBUMS -> if (albums.isEmpty()) EmptyHint("No albums") else
                    AlbumGrid(albums, listTop, listBottom, onOpenGroup)

                Section.ARTISTS -> if (artists.isEmpty()) EmptyHint("No artists") else
                    ArtistList(artists, listTop, listBottom, onOpenGroup)

                Section.PLAYLISTS -> if (playlists.isEmpty()) EmptyHint("No playlists yet") else
                    PlaylistList(playlists, listTop, listBottom, onOpenPlaylist)
            }
        }

        // header floats above the list on a soft fade so content scrolls underneath
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to AuraColors.bg.copy(alpha = 0.94f),
                        0.8f to AuraColors.bg.copy(alpha = 0.7f),
                        1f to Color.Transparent
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = top + 12.dp, bottom = 18.dp)
        ) {
            AnimatedContent(
                targetState = section,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "title"
            ) { target ->
                AuraHeader(
                    title = target.title,
                    subtitle = subtitle,
                    searchOpen = searchOpen,
                    query = query,
                    accent = accent,
                    onQuery = onQuery,
                    onToggleSearch = onToggleSearch
                )
            }
        }
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
    bottom: androidx.compose.ui.unit.Dp,
    controller: AuraController,
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
            topPadding = top + 72.dp,
            bottomPadding = bottom,
            onPlay = { index -> controller.play(tracks, index) },
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = top + 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(AuraIcons.Back, onBack, description = "Back")
        }
    }
}
