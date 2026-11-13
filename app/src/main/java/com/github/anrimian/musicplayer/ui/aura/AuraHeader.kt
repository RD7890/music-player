package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Height of the floating header pill. Lists use [HEADER_HEIGHT] to keep their first row clear of it. */
val HEADER_HEIGHT = 64.dp

/**
 * Fixed floating glass pill at the top of the screen, same shape language as the [MiniPlayer].
 *
 * - [onBack] null: the left icon shows [icon] (section icon). Set: it becomes a back arrow.
 * - [onSearch] set: the right button jumps somewhere else (used by Home).
 * - [onQuery] set: the right button slides the title out and an inline search field in.
 *
 * The title and the search field are both always composed and cross-slide with one animated
 * value drawn in a graphics layer, so opening search never re-lays out or recomposes the pill.
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
    val focusManager = LocalFocusManager.current
    val canSearch = onQuery != null

    val searchP by animateFloatAsState(
        targetValue = if (searchOpen && canSearch) 1f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "headerSearch"
    )

    LaunchedEffect(focusRequest, resetKey) {
        if (focusRequest && canSearch) {
            searchOpen = true
            onFocusHandled()
        }
    }
    LaunchedEffect(searchOpen) {
        if (searchOpen && canSearch) {
            delay(120) // let the slide start before the keyboard asks for layout
            try {
                focus.requestFocus()
            } catch (ignored: Exception) {
            }
        } else {
            focusManager.clearFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HEADER_HEIGHT)
            .glass(RoundedCornerShape(32.dp), base = Color.Black.copy(alpha = 0.55f))
            .padding(start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val lead = Modifier.size(48.dp)
        Box(
            modifier = if (onBack != null) lead.bounceClick(onBack) else lead,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (onBack != null) AuraIcons.Back else icon,
                contentDescription = if (onBack != null) "Back" else null,
                tint = AuraColors.accent,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(8.dp))

        Box(Modifier.weight(1f).height(HEADER_HEIGHT)) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = 1f - searchP
                        translationX = -28.dp.toPx() * searchP
                    },
                verticalArrangement = Arrangement.Center
            ) {
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
            if (onQuery != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = searchP
                            translationX = 28.dp.toPx() * (1f - searchP)
                        }
                ) {
                    if (query.isEmpty()) {
                        Text(
                            placeholder,
                            color = AuraColors.muted,
                            fontSize = 16.sp,
                            maxLines = 1,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQuery,
                        enabled = searchOpen,
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(AuraColors.accent),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth()
                            .focusRequester(focus)
                    )
                }
            }
        }

        if (onQuery != null) {
            Spacer(Modifier.width(8.dp))
            HeaderAction(open = searchOpen) {
                if (searchOpen) {
                    onQuery("")
                    searchOpen = false
                } else {
                    searchOpen = true
                }
            }
        } else if (onSearch != null) {
            Spacer(Modifier.width(8.dp))
            HeaderAction(open = false, onClick = onSearch)
        }
    }
}

/** Round blue button: magnifier, or a cross while the search field is open. */
@Composable
private fun HeaderAction(open: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .bounceClick(onClick)
            .glassFill(CircleShape, AuraColors.accent),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = open, animationSpec = tween(200), label = "headerActionIcon") { isOpen ->
            Icon(
                imageVector = if (isOpen) AuraIcons.Close else AuraIcons.Search,
                contentDescription = if (isOpen) "Close search" else "Search",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
