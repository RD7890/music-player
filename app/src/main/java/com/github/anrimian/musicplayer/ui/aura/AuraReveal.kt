package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

// Material "emphasized" curves: fast start and long soft landing on open, quick and clean on close.
private val OpenEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val CloseEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

/**
 * Opens the full player out of the floating mini player: the pill itself grows to full screen
 * (corners flatten, no overshoot) while the player content rises and fades in. Closing plays it
 * backwards, so the player shrinks back into the pill. Geometry matches [MiniPlayer]
 * (72dp tall, 16dp margins).
 *
 * [keepComposed] keeps the player composed (but unplaced, so invisible and untouchable) while a
 * song exists. Composing the whole player and loading its cover used to happen during the first
 * frames of the animation, which is what made it stutter; now that work is already done.
 */
@Composable
fun PlayerReveal(
    visible: Boolean,
    keepComposed: Boolean,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (visible) 1f else 0f) }
    LaunchedEffect(visible) {
        progress.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = if (visible) tween(420, easing = OpenEasing) else tween(280, easing = CloseEasing)
        )
    }
    if (!keepComposed && !visible && progress.value == 0f) return

    val density = LocalDensity.current
    val side = with(density) { 16.dp.toPx() }
    val pillHeight = with(density) { 72.dp.toPx() }
    val pillBottom = with(density) { (bottomInset + 16.dp).toPx() }
    val pillRadius = with(density) { 36.dp.toPx() }
    val rise = with(density) { 48.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            // unplaced while fully closed: not drawn, no touches, but already composed and loaded
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    if (visible || progress.value > 0f) placeable.place(0, 0)
                }
            }
            .graphicsLayer {
                val t = progress.value
                val h = size.height
                val left = lerp(side, 0f, t)
                val top = lerp(h - pillBottom - pillHeight, 0f, t)
                val bottom = lerp(h - pillBottom, h, t)
                val radius = lerp(pillRadius, 0f, t)
                val rect = RoundRect(left, top, size.width - left, bottom, CornerRadius(radius))
                shape = object : Shape {
                    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
                        Outline.Rounded(rect)
                }
                clip = true
            }
            .background(AuraColors.card)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val t = progress.value
                    alpha = ((t - 0.25f) / 0.5f).coerceIn(0f, 1f)
                    translationY = (1f - t) * rise
                }
        ) {
            content()
        }
    }
}
