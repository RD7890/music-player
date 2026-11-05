package com.github.anrimian.musicplayer.ui.aura

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.ui.common.images.glide.GlideApp
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

object AuraColors {
    val bg = Color(0xFF0E0E10)
    val card = Color(0xFF18181B)
    val text = Color(0xFFF4F2EC)
    val muted = Color(0xFF8C8A85)

    // flat tile palette: lime is the brand color, the rest colour the home tiles
    val brand = Color(0xFFD6F36B)
    val orange = Color(0xFFFF9166)
    val butter = Color(0xFFFFD86B)
    val mint = Color(0xFF7FE3C0)
    val cream = Color(0xFFEDE7DA)
    val ink = Color(0xFF141414)
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

fun fallbackAccent(id: Long?): Color {
    val raw = (((id ?: 0L) * 47L) % 360L).toFloat()
    val hue = if (raw < 0f) raw + 360f else raw
    return Color.hsv(hue, 0.62f, 0.92f)
}

/** Flat, muted tile color used while a cover is missing or loading. */
fun placeholderColor(id: Long?): Color {
    val raw = (((id ?: 0L) * 47L) % 360L).toFloat()
    val hue = if (raw < 0f) raw + 360f else raw
    return Color.hsv(hue, 0.45f, 0.5f)
}

/** Screen background: the dark base color tinted with the current accent, flat. */
fun screenTint(accent: Color, amount: Float = 0.17f): Color = lerp(AuraColors.bg, accent, amount)

private fun dominantColor(bmp: Bitmap): Color? {
    return try {
        var r = 0.0
        var g = 0.0
        var b = 0.0
        var weightSum = 0.0
        val hsv = FloatArray(3)
        for (y in 0 until bmp.height step 2) {
            for (x in 0 until bmp.width step 2) {
                val px = bmp.getPixel(x, y)
                android.graphics.Color.colorToHSV(px, hsv)
                val w = (hsv[1] * hsv[2]).toDouble() + 0.05
                r += android.graphics.Color.red(px) * w
                g += android.graphics.Color.green(px) * w
                b += android.graphics.Color.blue(px) * w
                weightSum += w
            }
        }
        if (weightSum <= 0.0) return null
        val avg = android.graphics.Color.rgb(
            (r / weightSum).toInt().coerceIn(0, 255),
            (g / weightSum).toInt().coerceIn(0, 255),
            (b / weightSum).toInt().coerceIn(0, 255)
        )
        android.graphics.Color.colorToHSV(avg, hsv)
        hsv[1] = hsv[1].coerceIn(0.45f, 0.9f)
        hsv[2] = hsv[2].coerceIn(0.6f, 0.95f)
        Color(android.graphics.Color.HSVToColor(hsv))
    } catch (e: Exception) {
        null
    }
}

/** Accent color taken from the current cover, falls back to a stable hue per track. */
@Composable
fun rememberAccent(composition: CompositionModel?): Color {
    val context = LocalContext.current
    val id = composition?.id
    val coverTime = composition?.coverModifyTime
    val color = remember { mutableStateOf(fallbackAccent(id)) }
    DisposableEffect(id, coverTime) {
        color.value = fallbackAccent(id)
        if (composition == null) {
            onDispose { }
        } else {
            val target = object : CustomTarget<Bitmap>(32, 32) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    dominantColor(resource)?.let { color.value = it }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            }
            GlideApp.with(context).asBitmap().load(composition.coverModel()).into(target)
            onDispose { GlideApp.with(context).clear(target) }
        }
    }
    return color.value
}

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
            tint = Color.White.copy(alpha = 0.55f),
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
    tint: Color = Color.White,
    description: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .bounceClick(onClick)
            .clip(CircleShape)
            .background(container),
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
