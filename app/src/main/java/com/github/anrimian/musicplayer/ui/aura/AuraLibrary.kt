package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.domain.models.playlist.PlaylistEntry

@Composable
fun AuraHeader(
    title: String,
    subtitle: String,
    searchOpen: Boolean,
    query: String,
    accent: Color,
    onQuery: (String) -> Unit,
    onToggleSearch: () -> Unit,
    modifier: Modifier = Modifier,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onAction: () -> Unit = {},
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            try {
                focus.requestFocus()
            } catch (ignored: Exception) {
            }
        }
    }
    Column(modifier = modifier.fillMaxWidth().animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                )
                Text(text = subtitle, color = AuraColors.muted, fontSize = 13.sp)
            }
            if (actionIcon != null) {
                RoundIconButton(
                    icon = actionIcon,
                    onClick = onAction,
                    container = accent,
                    tint = Color.Black.copy(alpha = 0.85f),
                    description = "New"
                )
                Spacer(Modifier.width(10.dp))
            }
            RoundIconButton(
                icon = if (searchOpen) AuraIcons.Close else AuraIcons.Search,
                onClick = onToggleSearch,
                description = "Search"
            )
        }
        AnimatedVisibility(
            visible = searchOpen,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(AuraIcons.Search, null, tint = AuraColors.muted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Search your music", color = AuraColors.muted, fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQuery,
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus)
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRow(
    item: CompositionModel,
    active: Boolean,
    playing: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) Modifier.bounceCombinedClick(onClick, onLongClick)
                else Modifier.bounceClick(onClick)
            )
            .clip(shape)
            .background(if (active) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (active) accent.copy(alpha = 0.55f) else Color.Transparent, shape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraCover(item, Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)), px = 160)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = if (active) Color.White else AuraColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.artist.orEmpty().ifBlank { "Unknown artist" },
                color = AuraColors.muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(10.dp))
        if (active) {
            EqBars(active = playing, color = accent, modifier = Modifier.padding(end = 8.dp))
        } else {
            Text(
                text = formatTime(item.duration),
                color = AuraColors.muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

private fun trackKey(item: CompositionModel): String {
    return if (item is PlaylistEntry) "e" + item.entryId else "c" + item.id
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackList(
    tracks: List<CompositionModel>,
    currentId: Long?,
    playing: Boolean,
    accent: Color,
    topPadding: Dp,
    bottomPadding: Dp,
    onPlay: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (Int) -> Unit = {},
    header: @Composable () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "header") { header() }
        itemsIndexed(tracks, key = { _, item -> trackKey(item) }) { index, item ->
            TrackRow(
                item = item,
                active = item.id == currentId,
                playing = playing,
                accent = accent,
                onClick = { onPlay(index) },
                modifier = Modifier.animateItem(),
                onLongClick = { onLongPress(index) }
            )
        }
    }
}

@Composable
fun GroupCard(
    group: TrackGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.bounceClick(onClick)) {
        AuraCover(
            composition = group.tracks.firstOrNull(),
            modifier = Modifier
                .fillMaxWidth()
                .aspectSquare()
                .clip(RoundedCornerShape(30.dp)),
            px = 400,
            iconFraction = 0.3f
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = group.title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        Text(
            text = group.subtitle,
            color = AuraColors.muted,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

private fun Modifier.aspectSquare(): Modifier = this.then(Modifier.aspectRatio(1f))

@Composable
fun AlbumGrid(
    groups: List<TrackGroup>,
    topPadding: Dp,
    bottomPadding: Dp,
    onOpen: (TrackGroup) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        gridItems(groups, key = { it.key }) { group ->
            GroupCard(group, onClick = { onOpen(group) })
        }
    }
}

@Composable
fun ArtistList(
    groups: List<TrackGroup>,
    topPadding: Dp,
    bottomPadding: Dp,
    onOpen: (TrackGroup) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(groups, key = { _, g -> g.key }) { _, group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onOpen(group) }
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraCover(group.tracks.firstOrNull(), Modifier.size(58.dp).clip(CircleShape), px = 160)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = group.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = group.subtitle, color = AuraColors.muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PlaylistList(
    playlists: List<Playlist>,
    topPadding: Dp,
    bottomPadding: Dp,
    onOpen: (Playlist) -> Unit,
    onLongPress: (Playlist) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(playlists, key = { _, p -> p.id }) { _, playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceCombinedClick({ onOpen(playlist) }, { onLongPress(playlist) })
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(placeholderColor(playlist.id)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(AuraIcons.Playlists, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val count = playlist.compositionsCount
                    Text(
                        text = count.toString() + if (count == 1) " song" else " songs",
                        color = AuraColors.muted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHint(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = AuraColors.muted,
            fontSize = 15.sp,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
fun DetailHeader(
    title: String,
    subtitle: String,
    cover: CompositionModel?,
    accent: Color,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuraCover(
            composition = cover,
            modifier = Modifier.size(190.dp).clip(RoundedCornerShape(44.dp)),
            px = 500,
            iconFraction = 0.35f
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = subtitle, color = AuraColors.muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillButton("Play", AuraIcons.Play, accent, Color.Black.copy(alpha = 0.85f), onPlay)
            PillButton("Shuffle", AuraIcons.Shuffle, Color.White.copy(alpha = 0.1f), Color.White, onShuffle)
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun PillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .bounceClick(onClick)
            .clip(RoundedCornerShape(26.dp))
            .background(container)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = content, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = content, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
