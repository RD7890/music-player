package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Floating pill: spinning cover with progress ring, title, play/pause. Swipe sideways to skip. */
@Composable
fun MiniPlayer(
    controller: AuraController,
    composition: CompositionModel,
    isPlaying: Boolean,
    accent: Color,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val position by controller.position.asState(0L)
    val duration = composition.duration
    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val spin = rememberSpin(isPlaying, 14000)
    var dragX by remember { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(36.dp)

    Row(
        modifier = modifier
            .height(72.dp)
            .bounceClick(onOpen)
            .clip(shape)
            .background(Color(0xFF181826).copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragX > 120f) controller.player.skipToPrevious()
                        else if (dragX < -120f) controller.player.skipToNext()
                        dragX = 0f
                    },
                    onDragCancel = { dragX = 0f },
                    onHorizontalDrag = { _, amount -> dragX += amount }
                )
            }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 3.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            AuraCover(
                composition = composition,
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer { rotationZ = spin.value }
                    .clip(CircleShape),
                px = 140
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = composition.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = composition.artist.orEmpty().ifBlank { "Unknown artist" },
                color = AuraColors.muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .bounceClick { controller.player.playOrPause() }
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isPlaying, animationSpec = tween(180), label = "miniPlay") { playing ->
                Icon(
                    imageVector = if (playing) AuraIcons.Pause else AuraIcons.Play,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Navigation without a bottom bar: one round orb. Tap it and the four sections fan out
 * along an arc around it. The orb itself always shows the section you are in.
 */
@Composable
fun OrbNav(
    open: Boolean,
    section: Section,
    accent: Color,
    onToggle: () -> Unit,
    onSelect: (Section) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = Section.entries
    val glow by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "orbGlow"
    )
    Box(modifier = modifier.size(72.dp), contentAlignment = Alignment.Center) {
        sections.forEachIndexed { index, item ->
            FanItem(
                item = item,
                index = index,
                count = sections.size,
                open = open,
                selected = item == section,
                accent = accent,
                onClick = { onSelect(item) }
            )
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    rotationZ = glow * 90f
                }
                .bounceClick(onToggle)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(accent, AuraColors.pink)))
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = open, animationSpec = tween(200), label = "orbIcon") { isOpen ->
                Icon(
                    imageVector = if (isOpen) AuraIcons.Close else section.icon,
                    contentDescription = "Navigation",
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer { rotationZ = -glow * 90f }
                )
            }
        }
    }
}

@Composable
private fun FanItem(
    item: Section,
    index: Int,
    count: Int,
    open: Boolean,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(open) {
        delay(if (open) index * 45L else (count - index) * 20L)
        progress.animateTo(
            targetValue = if (open) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
        )
    }
    // fan from straight up (90 deg) to straight left (180 deg), measured counter-clockwise
    val angle = Math.toRadians(95.0 + index * (80.0 / (count - 1)))
    val radius = 116f
    Column(
        modifier = Modifier
            .offset {
                val r = radius * progress.value
                IntOffset(
                    (cos(angle) * r * density).roundToInt(),
                    (-sin(angle) * r * density).roundToInt()
                )
            }
            .graphicsLayer {
                val p = progress.value.coerceIn(0f, 1.2f)
                scaleX = p
                scaleY = p
                alpha = progress.value.coerceIn(0f, 1f)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RoundIconButton(
            icon = item.icon,
            onClick = onClick,
            size = 54.dp,
            iconSize = 26.dp,
            container = if (selected) accent else Color(0xFF232336),
            tint = if (selected) Color.Black.copy(alpha = 0.85f) else Color.White,
            description = item.title
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
