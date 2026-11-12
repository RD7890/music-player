package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Height of the floating header pill. Lists use [HEADER_HEIGHT] to keep their first row clear of it. */
val HEADER_HEIGHT = 64.dp

/**
 * Fixed floating pill at the top of the screen, the same shape language as the [MiniPlayer]
 * at the bottom: round icon on the left, title + subtitle, round red button on the right.
 *
 * - [onLeading] null: the left circle shows [icon] (section icon).
 * - [onLeading] set: the left circle becomes a back button.
 * - [onSearch] set: the right button jumps somewhere else (used by Home).
 * - [onQuery] set: the right button turns the pill into an inline search field.
 * - neither: no right button.
 */
@Composable
fun FloatingHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    query: String = "",
    onQuery: ((String) -> Unit)? = null,
    placeholder: String = "Search",
    focusRequest: Boolean = false,
    onFocusHandled: () -> Unit = {},
    resetKey: Any? = null,
) {
    var searchOpen by remember(resetKey) { mutableStateOf(query.isNotEmpty()) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(focusRequest, resetKey) {
        if (focusRequest && onQuery != null) {
            searchOpen = true
            onFocusHandled()
        }
    }
    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            try {
                focus.requestFocus()
            } catch (ignored: Exception) {
            }
        }
    }

    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HEADER_HEIGHT)
            .glass(shape, base = Color.Black.copy(alpha = 0.55f))
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RingBadge(icon = icon, onClick = onBack)
        Spacer(Modifier.width(12.dp))

        Crossfade(
            targetState = searchOpen && onQuery != null,
            animationSpec = tween(160),
            modifier = Modifier.weight(1f),
            label = "headerBody"
        ) { searching ->
            if (searching && onQuery != null) {
                Box(Modifier.fillMaxWidth().height(HEADER_HEIGHT), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(placeholder, color = AuraColors.muted, fontSize = 16.sp, maxLines = 1)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQuery,
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(AuraColors.red),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus)
                    )
                }
            } else {
                Column(Modifier.fillMaxWidth(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            color = AuraColors.muted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        when {
            onQuery != null -> {
                Spacer(Modifier.width(8.dp))
                if (searchOpen) {
                    RoundIconButton(
                        icon = AuraIcons.Close,
                        onClick = {
                            onQuery("")
                            searchOpen = false
                        },
                        size = 48.dp,
                        iconSize = 22.dp,
                        container = AuraColors.red,
                        tint = Color.Black.copy(alpha = 0.85f),
                        description = "Close search"
                    )
                } else {
                    RoundIconButton(
                        icon = AuraIcons.Search,
                        onClick = { searchOpen = true },
                        size = 48.dp,
                        iconSize = 22.dp,
                        container = AuraColors.red,
                        tint = Color.Black.copy(alpha = 0.85f),
                        description = "Search"
                    )
                }
            }

            onSearch != null -> {
                Spacer(Modifier.width(8.dp))
                RoundIconButton(
                    icon = AuraIcons.Search,
                    onClick = onSearch,
                    size = 48.dp,
                    iconSize = 22.dp,
                    container = AuraColors.red,
                    tint = Color.Black.copy(alpha = 0.85f),
                    description = "Search"
                )
            }
        }
    }
}

/** Left circle: red ring (like the mini player's progress ring) around a section icon or back arrow. */
@Composable
private fun RingBadge(icon: ImageVector, onClick: (() -> Unit)?) {
    val base = Modifier.size(48.dp)
    Box(
        modifier = if (onClick != null) base.bounceClick(onClick).clip(CircleShape) else base,
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            drawArc(
                color = AuraColors.red,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke)
            )
        }
        Icon(
            imageVector = if (onClick != null) AuraIcons.Back else icon,
            contentDescription = if (onClick != null) "Back" else null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
