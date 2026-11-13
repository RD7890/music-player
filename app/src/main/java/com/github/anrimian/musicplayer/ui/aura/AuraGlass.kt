package com.github.anrimian.musicplayer.ui.aura

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/*
 * Glass look. Compose cannot blur what is behind a view, so the glass is built from layers:
 * a translucent white gradient and a bright-to-faint edge highlight, over a dotted black screen.
 */

private val GlassEdge = Brush.linearGradient(
    listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.22f))
)

/** Frosted panel. [base] is an optional dark underlay for panels that sit above busy content. */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(28.dp),
    base: Color = Color.Transparent,
    strength: Float = 1f,
): Modifier = this
    .clip(shape)
    .background(base, shape)
    .background(
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.18f * strength), Color.White.copy(alpha = 0.05f * strength))
        ),
        shape
    )
    .border(1.dp, GlassEdge, shape)

/** Tinted glass, for red / white buttons and tiles. */
fun Modifier.glassFill(
    shape: Shape,
    color: Color,
    top: Float = 0.95f,
    bottom: Float = 0.65f,
): Modifier = this
    .clip(shape)
    .background(Brush.verticalGradient(listOf(color.copy(alpha = top), color.copy(alpha = bottom))), shape)
    .border(1.dp, GlassEdge, shape)

/** Picks [glass] for see-through colors and [glassFill] for solid ones. */
fun Modifier.glassFor(shape: Shape, color: Color): Modifier =
    if (color.alpha < 0.2f) {
        if (color == Color.Transparent) this.clip(shape) else this.glass(shape)
    } else {
        this.glassFill(shape, color)
    }

/**
 * Static screen background: near-black with a fine dot grid. No gradients, no animation, so it
 * costs nothing while scrolling, and the glass panels have a texture to sit on.
 */
@Composable
fun AuraBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(AuraColors.bg)
            .drawWithCache {
                val step = 24.dp.toPx()
                val cols = (size.width / step).toInt() + 1
                val rows = (size.height / step).toInt() + 1
                val points = ArrayList<Offset>(cols * rows)
                for (r in 0 until rows) {
                    // every other row is shifted half a step, so the grid reads as a soft diamond lattice
                    val shift = if (r % 2 == 0) 0f else step / 2f
                    for (c in 0 until cols) {
                        points.add(Offset(c * step + shift, r * step))
                    }
                }
                val dot = Color.White.copy(alpha = 0.10f)
                val width = 1.6.dp.toPx()
                onDrawBehind {
                    drawPoints(points, PointMode.Points, dot, width, StrokeCap.Round)
                }
            }
    )
}

/* ---------- pop-ups: smooth in AND out ---------- */

/**
 * Pop-ups used to be removed from the tree the moment their state went null, so the exit
 * animation never ran. Now each one owns a [progress] (0 = hidden, 1 = shown); [close] plays it
 * back to 0 and only then calls the real onDismiss.
 */
@Stable
class OverlayState(
    val progress: Animatable<Float, AnimationVector1D>,
    val close: () -> Unit,
)

@Composable
fun rememberOverlay(onDismiss: () -> Unit): OverlayState {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val latest by rememberUpdatedState(onDismiss)
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, spring(dampingRatio = 0.86f, stiffness = 420f))
    }
    val close = remember<() -> Unit> {
        {
            if (!closing) {
                closing = true
                scope.launch {
                    progress.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
                    latest()
                }
            }
        }
    }
    BackHandler(onBack = close)
    return remember { OverlayState(progress, close) }
}

/** Dim layer behind a pop-up; fades with the pop-up, tap closes it. */
@Composable
fun OverlayScrim(overlay: OverlayState) {
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Color.Black.copy(alpha = 0.62f * overlay.progress.value.coerceIn(0f, 1f)))
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                overlay.close()
            }
    )
}

/** Bottom sheet motion: slides up from the bottom edge, no overshoot. */
fun Modifier.sheetSlide(overlay: OverlayState): Modifier = graphicsLayer {
    translationY = (1f - overlay.progress.value.coerceIn(0f, 1f)) * size.height
}

/** Dialog motion: scales up from 88% with a fade. */
fun Modifier.dialogPop(overlay: OverlayState): Modifier = graphicsLayer {
    val p = overlay.progress.value
    val scale = 0.88f + 0.12f * p
    scaleX = scale
    scaleY = scale
    alpha = p.coerceIn(0f, 1f)
}

/** Rows inside a pop-up rise and fade in one after another. */
fun Modifier.overlayItem(overlay: OverlayState, index: Int): Modifier = graphicsLayer {
    val p = ((overlay.progress.value - 0.15f - index * 0.05f) / 0.5f).coerceIn(0f, 1f)
    alpha = p
    translationY = (1f - p) * 24f
}
