package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel

private val Ink = Color(0xFF000000)

/**
 * Home: a hero card for what is playing, a bento grid of colour tiles that open each section,
 * and a carousel of recently added songs. Everything is flat colour.
 */
@Composable
fun HomeScreen(
    songs: List<Composition>,
    albumCount: Int,
    artistCount: Int,
    playlistCount: Int,
    current: CompositionModel?,
    isPlaying: Boolean,
    accent: Color,
    topPadding: Dp,
    bottomPadding: Dp,
    controller: AuraController,
    onOpenSection: (Section) -> Unit,
    onOpenPlayer: () -> Unit,
) {
    val recent = remember(songs) { songs.sortedByDescending { it.addedTime }.take(15) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item(key = "hero") {
            HeroCard(
                current = current,
                isPlaying = isPlaying,
                accent = accent,
                onClick = {
                    if (current != null) onOpenPlayer() else controller.shuffleAll(songs)
                },
                onPlayPause = { controller.player.playOrPause() },
                onShuffle = { controller.shuffleAll(songs) },
                modifier = Modifier.staggerIn(1)
            )
        }
        item(key = "bento") {
            Bento(
                songCount = songs.size,
                albumCount = albumCount,
                artistCount = artistCount,
                playlistCount = playlistCount,
                onOpenSection = onOpenSection,
                modifier = Modifier.staggerIn(2)
            )
        }
        if (recent.isNotEmpty()) {
            item(key = "recent") {
                Column(Modifier.staggerIn(3)) {
                    Text(
                        text = "Recently added",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        itemsIndexed(recent, key = { _, c -> c.id }) { index, song ->
                            Column(
                                Modifier
                                    .width(138.dp)
                                    .bounceClick { controller.play(recent, index) }
                            ) {
                                AuraCover(
                                    composition = song,
                                    modifier = Modifier.size(138.dp).clip(RoundedCornerShape(32.dp)),
                                    px = 320
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = song.artist.orEmpty().ifBlank { "Unknown artist" },
                                    color = AuraColors.muted,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    current: CompositionModel?,
    isPlaying: Boolean,
    accent: Color,
    onClick: () -> Unit,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (current != null) accent else AuraColors.brand
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(214.dp)
            .bounceClick(onClick)
            .glassFill(RoundedCornerShape(40.dp), container)
    ) {
        // flat decorative rings, like the grooves of a record
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.94f, size.height * 0.52f)
            val tone = Color.Black.copy(alpha = 0.06f)
            drawCircle(tone, radius = size.height * 0.98f, center = center)
            drawCircle(tone, radius = size.height * 0.66f, center = center)
            drawCircle(tone, radius = size.height * 0.34f, center = center)
        }
        if (current != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .size(128.dp)
                    .graphicsLayer { rotationZ = -7f }
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .offset(x = 8.dp, y = 8.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                )
                AuraCover(
                    composition = current,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(34.dp)),
                    px = 400
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp, end = 168.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EqBars(active = isPlaying, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "NOW PLAYING" else "PAUSED",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = current.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = current.artist.orEmpty().ifBlank { "Unknown artist" },
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .bounceClick(onPlayPause)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) AuraIcons.Pause else AuraIcons.Play,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = container,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(24.dp)
                    .fillMaxWidth(0.72f)
            ) {
                Text(
                    text = "START LISTENING",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Shuffle your whole library",
                    color = Color.White,
                    fontSize = 26.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(16.dp))
                PillButton("Shuffle all", AuraIcons.Shuffle, Color.White, container, onShuffle)
            }
        }
    }
}

@Composable
private fun Bento(
    songCount: Int,
    albumCount: Int,
    artistCount: Int,
    playlistCount: Int,
    onOpenSection: (Section) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(236.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoTile(
                title = "Songs",
                count = songCount.toString(),
                icon = Section.SONGS.icon,
                color = Color.White,
                tall = true,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onOpenSection(Section.SONGS) }
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoTile(
                    title = "Albums",
                    count = albumCount.toString(),
                    icon = Section.ALBUMS.icon,
                    color = Color.White.copy(alpha = 0.1f),
                    onColor = Color.White,
                    tall = false,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onClick = { onOpenSection(Section.ALBUMS) }
                )
                BentoTile(
                    title = "Artists",
                    count = artistCount.toString(),
                    icon = Section.ARTISTS.icon,
                    color = Color.White.copy(alpha = 0.1f),
                    onColor = Color.White,
                    tall = false,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onClick = { onOpenSection(Section.ARTISTS) }
                )
            }
        }
        BentoTile(
            title = "Playlists",
            count = playlistCount.toString(),
            icon = Section.PLAYLISTS.icon,
            color = Color.White.copy(alpha = 0.1f),
            onColor = Color.White,
            tall = false,
            modifier = Modifier.fillMaxWidth().height(96.dp),
            onClick = { onOpenSection(Section.PLAYLISTS) }
        )
    }
}

@Composable
private fun BentoTile(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    tall: Boolean,
    modifier: Modifier = Modifier,
    onColor: Color = Ink,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .bounceClick(onClick)
            .glassFor(RoundedCornerShape(34.dp), color)
            .padding(20.dp)
    ) {
        Column(Modifier.align(Alignment.TopStart)) {
            Text(
                text = title,
                color = onColor,
                fontSize = if (tall) 22.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = count,
                color = onColor.copy(alpha = 0.6f),
                fontSize = if (tall) 15.sp else 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AuraColors.blue,
            modifier = Modifier
                .align(if (tall) Alignment.BottomStart else Alignment.BottomEnd)
                .size(if (tall) 72.dp else 34.dp)
                .offset(y = if (tall) 6.dp else 6.dp)
        )
    }
}
