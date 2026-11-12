package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

/**
 * Opens the full player out of the floating mini player: the pill itself grows to full screen
 * (corners flatten, no overshoot), then the player content fades in. Closing plays it backwards,
 * so the player shrinks back into the pill. Geometry matches [MiniPlayer] (72dp tall, 16dp margins).
 */
@Composable
fun PlayerReveal(
    visible: Boolean,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (visible) 1f else 0f) }
    LaunchedEffect(visible) {
        progress.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(if (visible) 440 else 320, easing = FastOutSlowInEasing)
        )
    }
    if (!visible && progress.value == 0f) return

    val density = LocalDensity.current
    val side = with(density) { 16.dp.toPx() }
    val pillHeight = with(density) { 72.dp.toPx() }
    val pillBottom = with(density) { (bottomInset + 16.dp).toPx() }
    val pillRadius = with(density) { 36.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
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
                .graphicsLayer { alpha = ((progress.value - 0.3f) / 0.5f).coerceIn(0f, 1f) }
        ) {
            content()
        }
    }
}
