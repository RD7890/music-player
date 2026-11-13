package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist

private val SheetBase = Color(0xD9060304)
private val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
private val DangerColor = Color.White

class ActionItem(
    val icon: ImageVector,
    val label: String,
    val danger: Boolean = false,
    val onClick: () -> Unit,
)

/** Glass bottom sheet with a header and a list of actions. Slides in and out. */
@Composable
fun ActionSheet(
    title: String,
    subtitle: String,
    cover: CompositionModel?,
    accent: Color,
    items: List<ActionItem>,
    onDismiss: () -> Unit,
) {
    val overlay = rememberOverlay(onDismiss)
    val close = overlay.close
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        OverlayScrim(overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .sheetSlide(overlay)
                .glass(SheetShape, base = SheetBase)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = bottom + 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .overlayItem(overlay, 0)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cover != null) {
                    AuraCover(cover, Modifier.size(56.dp).glass(RoundedCornerShape(16.dp), strength = 0.4f), px = 160)
                    Spacer(Modifier.width(14.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = AuraColors.muted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .overlayItem(overlay, index + 1)
                        .bounceClick {
                            item.onClick()
                            close()
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.danger) DangerColor else accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = item.label,
                        color = if (item.danger) DangerColor else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/** Glass dialog drawn in the tree (not a platform Dialog) so it can animate out. [content] gets `close`. */
@Composable
fun AuraDialog(onDismiss: () -> Unit, content: @Composable (close: () -> Unit) -> Unit) {
    val overlay = rememberOverlay(onDismiss)
    Box(Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.Center) {
        OverlayScrim(overlay)
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .dialogPop(overlay)
                .glass(RoundedCornerShape(32.dp), base = SheetBase)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(24.dp)
        ) {
            content(overlay.close)
        }
    }
}

@Composable
fun DialogButton(label: String, color: Color, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .bounceClick(onClick)
            .glassFor(RoundedCornerShape(22.dp), if (filled) color else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (filled) Color.Black else color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FlatField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focus: FocusRequester? = null,
) {
    Column(modifier) {
        Text(label, color = AuraColors.muted, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(18.dp), strength = 0.7f)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = if (enabled) Color.White else AuraColors.muted,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(accent),
                modifier = if (focus != null) Modifier.fillMaxWidth().focusRequester(focus) else Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initial: String,
    confirmLabel: String,
    accent: Color,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focus.requestFocus()
        } catch (ignored: Exception) {
        }
    }
    AuraDialog(onDismiss) { close ->
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        FlatField(text, { text = it }, label, accent, focus = focus)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            DialogButton("Cancel", Color.White, false, close)
            DialogButton(confirmLabel, accent, true) {
                if (text.isNotBlank()) {
                    onConfirm(text.trim())
                    close()
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    danger: Boolean,
    accent: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AuraDialog(onDismiss) { close ->
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        Text(message, color = AuraColors.muted, fontSize = 15.sp)
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            DialogButton("Cancel", Color.White, false, close)
            DialogButton(confirmLabel, if (danger) DangerColor else accent, true) {
                onConfirm()
                close()
            }
        }
    }
}

@Composable
fun RenameSongDialog(
    song: CompositionModel,
    controller: AuraController,
    accent: Color,
    onResult: (Throwable?) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(song.title) }
    var fileName by remember { mutableStateOf("") }
    var originalFileName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(song.id) {
        controller.loadFullComposition(song.id) { full ->
            if (full != null) {
                originalFileName = full.fileName
                fileName = full.fileName
            }
        }
    }
    AuraDialog(onDismiss) { close ->
        Text("Rename", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        FlatField(title, { title = it }, "Title", accent)
        Spacer(Modifier.height(12.dp))
        FlatField(fileName, { fileName = it }, "File name", accent, enabled = originalFileName != null)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            DialogButton("Cancel", Color.White, false, close)
            DialogButton("Save", accent, true) {
                val newTitle = title.trim().takeIf { it.isNotEmpty() && it != song.title }
                val newFile = fileName.trim().takeIf { it.isNotEmpty() && originalFileName != null && it != originalFileName }
                if (newTitle != null || newFile != null) {
                    controller.rename(song.id, newTitle, newFile, onResult)
                }
                close()
            }
        }
    }
}

@Composable
fun SongInfoDialog(song: CompositionModel, accent: Color, onDismiss: () -> Unit) {
    AuraDialog(onDismiss) { close ->
        Text("Details", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        InfoLine("Title", song.title)
        InfoLine("Artist", song.artist.orEmpty().ifBlank { "Unknown" })
        InfoLine("Album", song.album.orEmpty().ifBlank { "Unknown" })
        InfoLine("Duration", formatTime(song.duration))
        InfoLine("Size", String.format("%.1f MB", song.size / 1024f / 1024f))
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DialogButton("Close", accent, true, close)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Text(label, color = AuraColors.muted, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Playlist picker with a "new playlist" row on top. */
@Composable
fun PlaylistPickerSheet(
    playlists: List<Playlist>,
    accent: Color,
    onPick: (Playlist) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val overlay = rememberOverlay(onDismiss)
    val close = overlay.close
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        OverlayScrim(overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .sheetSlide(overlay)
                .glass(SheetShape, base = SheetBase)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(top = 22.dp, start = 16.dp, end = 16.dp, bottom = bottom + 16.dp)
        ) {
            Text(
                "Add to playlist",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .overlayItem(overlay, 0)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .overlayItem(overlay, 1)
                    .bounceClick {
                        onCreate()
                        close()
                    }
                    .glassFill(RoundedCornerShape(20.dp), accent)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(AuraIcons.Add, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "New playlist",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.overlayItem(overlay, 2),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick {
                                onPick(playlist)
                                close()
                            }
                            .glass(RoundedCornerShape(20.dp), strength = 0.7f)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(AuraIcons.Playlists, null, tint = accent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                playlist.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                playlist.compositionsCount.toString() + " songs",
                                color = AuraColors.muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
