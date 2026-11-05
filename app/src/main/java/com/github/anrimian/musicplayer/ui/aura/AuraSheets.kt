package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist

private val SheetColor = Color(0xFF151518)
private val DialogColor = Color(0xFF1B1B1F)
private val DangerColor = Color(0xFFFF6B6B)

class ActionItem(
    val icon: ImageVector,
    val label: String,
    val danger: Boolean = false,
    val onClick: () -> Unit,
)

/** Flat bottom sheet with a header and a list of actions. */
@Composable
fun ActionSheet(
    title: String,
    subtitle: String,
    cover: CompositionModel?,
    accent: Color,
    items: List<ActionItem>,
    onDismiss: () -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = shown, enter = fadeIn(tween(180)), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onDismiss()
                    }
            )
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(
                visible = shown,
                enter = slideInVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(SheetColor)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = bottom + 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (cover != null) {
                            AuraCover(cover, Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)), px = 160)
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
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick {
                                    onDismiss()
                                    item.onClick()
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
    }
}

@Composable
fun AuraDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(DialogColor)
                .padding(24.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DialogButton(label: String, color: Color, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .bounceClick(onClick)
            .clip(RoundedCornerShape(22.dp))
            .background(if (filled) color else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (filled) Color.Black.copy(alpha = 0.85f) else color,
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
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.08f))
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
    AuraDialog(onDismiss) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        FlatField(text, { text = it }, label, accent, focus = focus)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            DialogButton("Cancel", Color.White, false, onDismiss)
            DialogButton(confirmLabel, accent, true) {
                if (text.isNotBlank()) {
                    onConfirm(text.trim())
                    onDismiss()
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
    AuraDialog(onDismiss) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        Text(message, color = AuraColors.muted, fontSize = 15.sp)
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            DialogButton("Cancel", Color.White, false, onDismiss)
            DialogButton(confirmLabel, if (danger) DangerColor else accent, true) {
                onConfirm()
                onDismiss()
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
    AuraDialog(onDismiss) {
        Text("Rename", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(16.dp))
        FlatField(title, { title = it }, "Title", accent)
        Spacer(Modifier.height(12.dp))
        FlatField(fileName, { fileName = it }, "File name", accent, enabled = originalFileName != null)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            DialogButton("Cancel", Color.White, false, onDismiss)
            DialogButton("Save", accent, true) {
                val newTitle = title.trim().takeIf { it.isNotEmpty() && it != song.title }
                val newFile = fileName.trim().takeIf { it.isNotEmpty() && originalFileName != null && it != originalFileName }
                if (newTitle != null || newFile != null) {
                    controller.rename(song.id, newTitle, newFile, onResult)
                }
                onDismiss()
            }
        }
    }
}

@Composable
fun SongInfoDialog(song: CompositionModel, accent: Color, onDismiss: () -> Unit) {
    AuraDialog(onDismiss) {
        Text("Details", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        InfoLine("Title", song.title)
        InfoLine("Artist", song.artist.orEmpty().ifBlank { "Unknown" })
        InfoLine("Album", song.album.orEmpty().ifBlank { "Unknown" })
        InfoLine("Duration", formatTime(song.duration))
        InfoLine("Size", String.format("%.1f MB", song.size / 1024f / 1024f))
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DialogButton("Close", accent, true, onDismiss)
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
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = shown, enter = fadeIn(tween(180))) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onDismiss()
                    }
            )
        }
        Box(Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(
                visible = shown,
                enter = slideInVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(SheetColor)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .padding(top = 22.dp, start = 16.dp, end = 16.dp, bottom = bottom + 16.dp)
                ) {
                    Text(
                        "Add to playlist",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick {
                                onDismiss()
                                onCreate()
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(accent)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(AuraIcons.Add, null, tint = Color.Black.copy(alpha = 0.85f), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "New playlist",
                            color = Color.Black.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(playlists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick {
                                        onDismiss()
                                        onPick(playlist)
                                    }
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
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
    }
}
