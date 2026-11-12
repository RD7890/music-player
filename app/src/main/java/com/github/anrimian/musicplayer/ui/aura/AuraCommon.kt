package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.bumptech.glide.integration.compose.GlideImage
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

object AuraColors {
    // black, white, and one blue for accents and icons. Nothing here changes with the playing song.
    val blue = Color(0xFF0377FC)
    val bg = Color(0xFF000000)
    val card = Color(0xFF111111)
    val tile = Color(0xFF1C1C1E)
    val text = Color(0xFFFFFFFF)
    val muted = Color(0xFF9A9A9F)

    val brand = blue
    val white = Color(0xFFFFFFFF)
    val ink = Color(0xFF000000)
}

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AuraColors.brand,
            background = AuraColors.bg,
            surface = AuraColors.card,
            onSurface = AuraColors.text,
            onBackground = AuraColors.text,
        ),
        content = content
    )
}

/** Subscribes to an Rx observable for as long as the composable is alive. */
@Composable
fun <T : Any> Observable<T>.asState(initial: T): State<T> {
    val source = this
    val state = remember(source) { mutableStateOf(initial) }
    DisposableEffect(source) {
        val disposable = source
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ value -> state.value = value }, { })
        onDispose { disposable.dispose() }
    }
    return state
}

fun CompositionModel.coverModel() = CompositionImage(
    id,
    modifiedTime,
    coverModifyTime,
    size,
    isFileExists
)

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return minutes.toString() + ":" + seconds.toString().padStart(2, '0')
}

/** Flat tile color used while a cover is missing or loading. Same for every song. */
fun placeholderColor(id: Long?): Color = AuraColors.tile

/** Screen background: black base with a fixed tint. */
fun screenTint(accent: Color, amount: Float = 0.17f): Color = lerp(AuraColors.bg, accent, amount)

@Composable
fun AuraCover(
    composition: CompositionModel?,
    modifier: Modifier = Modifier,
    px: Int = 300,
    iconFraction: Float = 0.42f,
) {
    val seed = composition?.id
    Box(
        modifier = modifier.background(placeholderColor(seed)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = AuraIcons.Songs,
            contentDescription = null,
            tint = AuraColors.blue.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxSize(iconFraction)
        )
        if (composition != null) {
            GlideImage(
                model = composition.coverModel(),
                contentDescription = composition.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            ) { builder -> builder.override(px).timeout(5000) }
        }
    }
}

/** Spring press feedback + click, used on every tappable surface. */
fun Modifier.bounceClick(onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** Soft fade + rise for the first rows of a list, so screens feel like they are being laid out. */
fun Modifier.staggerIn(index: Int): Modifier = composed {
    val animate = index < 12
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animate) {
            delay(120L + index * 40L)
            progress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        }
    }
    this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 48f
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bounceCombinedClick(onClick: () -> Unit, onLongClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "pressLong"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = source,
            indication = null,
            onClick = onClick,
            onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            }
        )
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    container: Color = Color.White.copy(alpha = 0.08f),
    tint: Color = AuraColors.blue,
    description: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .bounceClick(onClick)
            .glassFor(CircleShape, container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun EqBars(active: Boolean, color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "eq")
    val a = transition.animateFloat(
        0.25f, 1f,
        infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), AnimRepeat.Reverse),
        label = "b0"
    )
    val b = transition.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(560, easing = FastOutSlowInEasing), AnimRepeat.Reverse),
        label = "b1"
    )
    val c = transition.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), AnimRepeat.Reverse),
        label = "b2"
    )
    val d = transition.animateFloat(
        0.3f, 0.9f,
        infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), AnimRepeat.Reverse),
        label = "b3"
    )
    val heights = listOf(a, b, c, d)
    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { h ->
            Box(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight(if (active) h.value else 0.2f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

/** Continuous rotation that freezes in place when [playing] becomes false. */
@Composable
fun rememberSpin(playing: Boolean, periodMs: Int): Animatable<Float, androidx.compose.animation.core.AnimationVector1D> {
    val angle = remember { Animatable(0f) }
    LaunchedEffect(playing) {
        if (playing) {
            while (true) {
                val start = angle.value % 360f
                angle.snapTo(start)
                angle.animateTo(start + 360f, tween(periodMs, easing = LinearEasing))
            }
        }
    }
    return angle
}
