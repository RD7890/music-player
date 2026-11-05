package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode as AnimRepeat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode as PlayRepeat
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import kotlin.math.roundToInt

private val SPEEDS = floatArrayOf(0.75f, 1f, 1.25f, 1.5f, 2f)

@Composable
fun PlayerScreen(
    controller: AuraController,
    composition: CompositionModel,
    isPlaying: Boolean,
    accent: Color,
    onClose: () -> Unit,
    onQueue: () -> Unit,
    onSleepTimer: () -> Unit,
    onEqualizer: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val dragY = remember { Animatable(0f) }
    val shuffle by controller.shuffle.asState(false)
    val repeat by controller.repeatMode.asState(PlayRepeat.NONE)
    val speed by controller.speed.asState(1f)
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, dragY.value.roundToInt()) }
            .background(screenTint(accent, 0.24f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (dragY.value > 280f) onClose() else dragY.animateTo(0f, spring())
                        }
                    },
                    onDragCancel = { scope.launch { dragY.animateTo(0f, spring()) } },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        scope.launch { dragY.snapTo((dragY.value + amount).coerceAtLeast(0f)) }
                    }
                )
            }
    ) {
        // flat background tinted with the cover accent
        Box(
            Modifier
                .fillMaxSize()
                .background(screenTint(accent, 0.24f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = top + 8.dp, bottom = bottom + 16.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(AuraIcons.Collapse, onClose, description = "Close player")
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = composition.album.orEmpty().ifBlank { "Your library" },
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                RoundIconButton(AuraIcons.Queue, onQueue, description = "Queue")
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val coverSize = minOf(maxWidth * 0.66f, maxHeight * 0.92f)
                VinylCover(composition, isPlaying, accent, coverSize)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = composition.title,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = composition.artist.orEmpty().ifBlank { "Unknown artist" },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                EqBars(active = isPlaying, color = accent, modifier = Modifier.padding(start = 12.dp, end = 8.dp))
                RoundIconButton(
                    icon = AuraIcons.AddToPlaylist,
                    onClick = onAddToPlaylist,
                    size = 44.dp,
                    description = "Add to playlist"
                )
            }

            Spacer(Modifier.height(14.dp))
            SeekBar(controller, composition.duration, accent)
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(
                    icon = AuraIcons.Shuffle,
                    onClick = { controller.player.changeRandomMode() },
                    container = if (shuffle) accent.copy(alpha = 0.3f) else Color.Transparent,
                    tint = if (shuffle) accent else Color.White.copy(alpha = 0.7f),
                    description = "Shuffle"
                )
                RoundIconButton(
                    icon = AuraIcons.SkipBack,
                    onClick = { controller.player.skipToPrevious() },
                    size = 56.dp,
                    iconSize = 28.dp,
                    description = "Previous"
                )
                PlayButton(isPlaying, accent) { controller.player.playOrPause() }
                RoundIconButton(
                    icon = AuraIcons.SkipForward,
                    onClick = { controller.player.skipToNext() },
                    size = 56.dp,
                    iconSize = 28.dp,
                    description = "Next"
                )
                val repeatActive = repeat != PlayRepeat.NONE
                RoundIconButton(
                    icon = if (repeat == PlayRepeat.REPEAT_COMPOSITION) AuraIcons.RepeatOnce else AuraIcons.Repeat,
                    onClick = { controller.player.changeRepeatMode() },
                    container = if (repeatActive) accent.copy(alpha = 0.3f) else Color.Transparent,
                    tint = if (repeatActive) accent else Color.White.copy(alpha = 0.7f),
                    description = "Repeat"
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                PillButton(
                    label = formatSpeed(speed),
                    icon = AuraIcons.Speed,
                    container = Color.White.copy(alpha = 0.1f),
                    content = Color.White,
                    onClick = { controller.player.setPlaybackSpeed(nextSpeed(speed)) }
                )
                PillButton("Sleep", AuraIcons.SleepTimer, Color.White.copy(alpha = 0.1f), Color.White, onSleepTimer)
                PillButton("EQ", AuraIcons.Equalizer, Color.White.copy(alpha = 0.1f), Color.White, onEqualizer)
            }
        }
    }
}

private fun nextSpeed(current: Float): Float {
    val index = SPEEDS.indexOfFirst { kotlin.math.abs(it - current) < 0.01f }
    return SPEEDS[(index + 1) % SPEEDS.size]
}

private fun formatSpeed(speed: Float): String {
    val rounded = (speed * 100).roundToInt() / 100f
    return (if (rounded == rounded.toInt().toFloat()) rounded.toInt().toString() else rounded.toString()) + "x"
}

/** Cover card with a record sliding out from behind it while the music plays. */
@Composable
private fun VinylCover(
    composition: CompositionModel,
    isPlaying: Boolean,
    accent: Color,
    size: androidx.compose.ui.unit.Dp,
) {
    val slide by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "vinylSlide"
    )
    val spin = rememberSpin(isPlaying, 9000)
    val transition = rememberInfiniteTransition(label = "breath")
    val breath by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), AnimRepeat.Reverse),
        label = "breathValue"
    )
    val scale = if (isPlaying) breath else 1f

    Box(contentAlignment = Alignment.Center) {
        // the record
        Canvas(
            modifier = Modifier
                .size(size)
                .offset(x = size * 0.24f * slide)
                .graphicsLayer { rotationZ = spin.value }
        ) {
            val r = this.size.minDimension / 2f
            drawCircle(Color(0xFF0C0C12), radius = r)
            for (i in 1..6) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = r * (0.34f + i * 0.095f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -40f,
                sweepAngle = 35f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = this.size
            )
            drawCircle(accent, radius = r * 0.3f)
            drawCircle(Color(0xFF0C0C12), radius = r * 0.05f)
        }
        // the cover
        AuraCover(
            composition = composition,
            modifier = Modifier
                .size(size)
                .offset(x = -(size * 0.1f) * slide)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(40.dp)),
            px = 700,
            iconFraction = 0.3f
        )
    }
}

@Composable
private fun PlayButton(isPlaying: Boolean, accent: Color, onClick: () -> Unit) {
    val corner by animateFloatAsState(
        targetValue = if (isPlaying) 30f else 44f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "playCorner"
    )
    Box(
        modifier = Modifier
            .size(88.dp)
            .bounceClick(onClick)
            .clip(RoundedCornerShape(corner.dp))
            .background(accent),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = isPlaying, animationSpec = tween(200), label = "playIcon") { playing ->
            Icon(
                imageVector = if (playing) AuraIcons.Pause else AuraIcons.Play,
                contentDescription = if (playing) "Pause" else "Play",
                tint = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun SeekBar(controller: AuraController, duration: Long, accent: Color) {
    val position by controller.position.asState(0L)
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val fraction = if (dragging) dragFraction
    else if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val shownPosition = if (dragging) (dragFraction * duration).toLong() else position

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        val f = (offset.x / size.width).coerceIn(0f, 1f)
                        controller.player.onSeekStarted()
                        controller.player.onSeekFinished((f * duration).toLong())
                    }
                }
                .pointerInput(duration) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            controller.player.onSeekStarted()
                        },
                        onDragEnd = {
                            controller.player.onSeekFinished((dragFraction * duration).toLong())
                            dragging = false
                        },
                        onDragCancel = { dragging = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(Modifier.fillMaxWidth().height(30.dp)) {
                val trackH = 6.dp.toPx()
                val y = size.height / 2f
                val radius = androidx.compose.ui.geometry.CornerRadius(trackH / 2f, trackH / 2f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.16f),
                    topLeft = Offset(0f, y - trackH / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width, trackH),
                    cornerRadius = radius
                )
                val w = size.width * fraction
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(0f, y - trackH / 2f),
                    size = androidx.compose.ui.geometry.Size(w.coerceAtLeast(trackH), trackH),
                    cornerRadius = radius
                )
                val thumbR = if (dragging) 10.dp.toPx() else 7.dp.toPx()
                drawCircle(Color.White, radius = thumbR, center = Offset(w, y))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(shownPosition), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(formatTime(duration), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

/** Up-next sheet: drag the handle to reorder, tap the cross to remove. */
@Composable
fun QueueSheet(
    controller: AuraController,
    isPlaying: Boolean,
    accent: Color,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val queue by controller.queue.asState(emptyList())
    val event by controller.queueEvent.asState(com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent(null))
    val currentItemId = event.playQueueItem?.itemId
    val local = remember { mutableStateListOf<PlayQueueItem>() }
    var confirmClear by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // follow the database when items are added or removed; pure reorders are already applied locally
    LaunchedEffect(queue) {
        val localIds = local.map { it.itemId }
        val newIds = queue.map { it.itemId }
        if (localIds.size != newIds.size || localIds.toSet() != newIds.toSet()) {
            local.clear()
            local.addAll(queue)
        }
    }
    LaunchedEffect(local.size, currentItemId) {
        val index = local.indexOfFirst { it.itemId == currentItemId }
        if (index >= 0) listState.scrollToItem((index - 1).coerceAtLeast(0))
    }

    val reorderable = rememberReorderableLazyListState(listState) { from, to ->
        val snapshot = local.toList()
        local.add(to.index, local.removeAt(from.index))
        controller.moveQueueItem(snapshot, from.index, to.index)
    }

    val shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 90.dp)
            .clip(shape)
            .background(Color(0xFF12121C))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Up next", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(local.size.toString() + " in queue · drag to reorder", color = AuraColors.muted, fontSize = 12.sp)
            }
            if (local.isNotEmpty()) {
                DialogButton("Clear", accent, false) { confirmClear = true }
            }
            RoundIconButton(AuraIcons.Close, onClose, description = "Close queue")
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 6.dp, bottom = bottom + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(local, key = { _, item -> item.itemId }) { _, item ->
                ReorderableItem(reorderable, key = item.itemId) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 10.dp else 0.dp, label = "dragElevation")
                    QueueRow(
                        item = item,
                        active = item.itemId == currentItemId,
                        playing = isPlaying,
                        accent = accent,
                        dragging = isDragging,
                        handle = Modifier.draggableHandle(),
                        onClick = { controller.player.skipToItem(item.itemId) },
                        onRemove = { controller.removeFromQueue(item) },
                        modifier = Modifier.shadow(elevation, RoundedCornerShape(22.dp))
                    )
                }
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Clear queue?",
            message = "Playback will stop and the queue will be emptied.",
            confirmLabel = "Clear",
            danger = true,
            accent = accent,
            onConfirm = { controller.clearQueue() },
            onDismiss = { confirmClear = false }
        )
    }
}

@Composable
private fun QueueRow(
    item: PlayQueueItem,
    active: Boolean,
    playing: Boolean,
    accent: Color,
    dragging: Boolean,
    handle: Modifier,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                when {
                    dragging -> Color(0xFF2A2A3D)
                    active -> accent.copy(alpha = 0.2f)
                    else -> Color.White.copy(alpha = 0.06f)
                }
            )
            .clickable(onClick = onClick)
            .padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraCover(item, Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)), px = 140)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color.White,
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
        if (active) {
            EqBars(active = playing, color = accent, modifier = Modifier.padding(horizontal = 8.dp))
        }
        RoundIconButton(
            icon = AuraIcons.Close,
            onClick = onRemove,
            size = 36.dp,
            iconSize = 18.dp,
            container = Color.Transparent,
            tint = AuraColors.muted,
            description = "Remove from queue"
        )
        Box(
            modifier = handle.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AuraIcons.DragHandle,
                contentDescription = "Drag to reorder",
                tint = AuraColors.muted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
