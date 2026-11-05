package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

private val SplashBg = Color(0xFF5B4BFF)
private val SplashDisc = Color(0xFF1B1633)
private val SplashGroove = Color(0xFF3A3260)
private val SplashLabel = Color(0xFFFF5C7A)

/**
 * Cold-start splash: the vinyl logo spins in on flat violet, then a circular hole grows from the
 * label and reveals the app underneath.
 */
@Composable
fun AuraSplash(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.82f) }
    val spin = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val reveal = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { logoScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)) }
            launch { spin.animateTo(360f, tween(1000, easing = FastOutSlowInEasing)) }
            launch {
                delay(300)
                textAlpha.animateTo(1f, tween(350))
            }
        }
        delay(200)
        launch { textAlpha.animateTo(0f, tween(200)) }
        reveal.animateTo(1f, tween(750, easing = CubicBezierEasing(0.6f, 0f, 0.2f, 1f)))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            val c = center
            drawRect(SplashBg)

            val r = 72.dp.toPx() * logoScale.value
            withTransform({ rotate(spin.value, c) }) {
                drawCircle(SplashDisc, r, c)
                drawCircle(SplashGroove, r * 0.70f, c, style = Stroke(1.5.dp.toPx()))
                drawCircle(SplashGroove, r * 0.54f, c, style = Stroke(1.5.dp.toPx()))
                for (ratio in floatArrayOf(0.83f, 0.63f)) {
                    val rr = r * ratio
                    drawArc(
                        color = Color.White,
                        startAngle = 205f,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(c.x - rr, c.y - rr),
                        size = androidx.compose.ui.geometry.Size(rr * 2, rr * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                drawCircle(SplashLabel, r * 0.35f, c)
                drawCircle(SplashBg, r * 0.09f, c)
            }

            // the reveal: a transparent circle growing from the record label
            val maxR = hypot(size.width, size.height) / 2f * 1.05f
            val hole = r * 0.35f + (maxR - r * 0.35f) * reveal.value
            if (reveal.value > 0f) {
                drawCircle(Color.Black, radius = hole, center = c, blendMode = BlendMode.Clear)
            }
        }
        Text(
            text = "Aura",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 128.dp)
                .graphicsLayer { alpha = textAlpha.value }
        )
    }
}
