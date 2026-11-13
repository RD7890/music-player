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
import androidx.compose.foundation.lazy.grid.GridItemSpan
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

/** Big editorial title with a count chip, followed by an always-visible search pill. */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    chip: String,
    placeholder: String,
    query: String,
    accent: Color,
    onQuery: (String) -> Unit,
    focusRequest: Boolean,
    onFocusHandled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(focusRequest) {
        if (focusRequest) {
            try {
                focus.requestFocus()
            } catch (ignored: Exception) {
            }
            onFocusHandled()
        }
    }
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = eyebrow.uppercase(),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
            }
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(chip, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .padding(start = 18.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(AuraIcons.Search, null, tint = AuraColors.accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(placeholder, color = AuraColors.muted, fontSize = 16.sp)
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
            if (query.isNotEmpty()) {
                RoundIconButton(
                    icon = AuraIcons.Close,
                    onClick = { onQuery("") },
                    size = 40.dp,
                    iconSize = 18.dp,
                    container = Color.White.copy(alpha = 0.1f),
                    description = "Clear"
                )
            }
        }
    }
}

/** Home variant: greeting + a two-line question, and a search pill that jumps to the songs screen. */
@Composable
fun HomeHeader(
    greeting: String,
    accent: Color,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)) {
        Text(
            text = greeting.uppercase(),
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            text = "What's the\nmood today?",
            color = Color.White,
            fontSize = 40.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bounceClick(onSearch)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(AuraIcons.Search, null, tint = AuraColors.accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Search your music", color = AuraColors.muted, fontSize = 16.sp)
        }
    }
}

@Composable
fun EmptyScreen(topPadding: Dp, text: String, header: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = topPadding)
    ) {
        header()
        EmptyHint(text)
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
    val shape = RoundedCornerShape(24.dp)
    val onCard = Color.Black
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) Modifier.bounceCombinedClick(onClick, onLongClick)
                else Modifier.bounceClick(onClick)
            )
            .then(if (active) Modifier.glassFill(shape, accent, 0.9f, 0.6f) else Modifier.glass(shape, strength = 0.75f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraCover(item, Modifier.size(56.dp).clip(RoundedCornerShape(17.dp)), px = 160)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = if (active) onCard else AuraColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.artist.orEmpty().ifBlank { "Unknown artist" },
                color = if (active) onCard.copy(alpha = 0.75f) else AuraColors.muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(10.dp))
        if (active) {
            EqBars(active = playing, color = onCard, modifier = Modifier.padding(end = 8.dp))
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
                modifier = Modifier.animateItem().staggerIn(index),
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
                .glass(RoundedCornerShape(30.dp), strength = 0.4f),
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
    header: @Composable () -> Unit,
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
        item(span = { GridItemSpan(maxLineSpan) }) { header() }
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
    header: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "header") { header() }
        itemsIndexed(groups, key = { _, g -> g.key }) { _, group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onOpen(group) }
                    .glass(RoundedCornerShape(28.dp), strength = 0.75f)
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
    header: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "header") { header() }
        itemsIndexed(playlists, key = { _, p -> p.id }) { _, playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceCombinedClick({ onOpen(playlist) }, { onLongPress(playlist) })
                    .glass(RoundedCornerShape(28.dp), strength = 0.75f)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .glass(RoundedCornerShape(20.dp), strength = 1.3f),
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
            modifier = Modifier.size(190.dp).glass(RoundedCornerShape(44.dp), strength = 0.4f),
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
            PillButton("Play", AuraIcons.Play, accent, Color.Black, onPlay)
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
            .glassFor(RoundedCornerShape(26.dp), container)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (container.alpha >= 0.2f) content else AuraColors.accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = content, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SongsHero(accent: Color, onPlayAll: () -> Unit, onShuffle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeroButton("Shuffle all", AuraIcons.Shuffle, accent, Color.Black, onShuffle, Modifier.weight(1f))
        HeroButton("Play all", AuraIcons.Play, Color.White.copy(alpha = 0.1f), Color.White, onPlayAll, Modifier.weight(1f))
    }
}

@Composable
private fun HeroButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .bounceClick(onClick)
            .glassFor(RoundedCornerShape(28.dp), container),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (container.alpha >= 0.2f) content else AuraColors.accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = content, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
