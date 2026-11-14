package com.github.anrimian.musicplayer.ui.aura

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle
import com.github.anrimian.musicplayer.R
import kotlin.math.max
import kotlin.math.min

/**
 * Aura glass look for the playback notification.
 *
 * A notification is drawn by the system, so real backdrop blur is impossible. Instead the card
 * background is rendered here as a bitmap: blurred cover, dark scrim, white sheen, dot screen and
 * a bright-to-faint edge, the same layers as Modifier.glass() in AuraGlass.kt.
 *
 * Hook-up: call [apply] on the builder your existing notifier already creates, right before build().
 * It replaces the small icon, actions, style and custom views. Keep your own content intent,
 * channel, ongoing flag and foreground-service code as they are.
 *
 * Layout: the card shows cover, title, artist and progress. The buttons are the system action
 * row (compact 0,1,2 = previous, play/pause, next) drawn with the Aura icons, so lock screen,
 * Android 13+ media controls, Bluetooth and Auto keep working through the MediaSession token.
 */
object AuraNotification {

    class State(
        val title: String,
        val artist: String,
        val cover: Bitmap?,
        val isPlaying: Boolean,
        val positionMs: Long = 0L,
        val durationMs: Long = 0L,
    )

    class Intents(
        val previous: PendingIntent,
        val playPause: PendingIntent,
        val next: PendingIntent,
        val close: PendingIntent? = null,
    )

    fun apply(
        builder: NotificationCompat.Builder,
        context: Context,
        state: State,
        intents: Intents,
        token: MediaSessionCompat.Token? = null,
    ): NotificationCompat.Builder {
        val density = context.resources.displayMetrics.density
        val cover = state.cover?.let { softwareCopy(it) }

        val collapsed = RemoteViews(context.packageName, R.layout.aura_notification_collapsed)
        bindCard(collapsed, context, state, cover, density, heightDp = 48, artDp = 40, artRadiusDp = 10)

        val expanded = RemoteViews(context.packageName, R.layout.aura_notification_expanded)
        bindCard(expanded, context, state, cover, density, heightDp = 104, artDp = 84, artRadiusDp = 16)
        bindProgress(expanded, state)

        builder.clearActions()
        builder.addAction(R.drawable.ic_aura_skip_back, context.getString(R.string.aura_n_previous), intents.previous)
        builder.addAction(
            if (state.isPlaying) R.drawable.ic_aura_pause else R.drawable.ic_aura_play,
            context.getString(if (state.isPlaying) R.string.aura_n_pause else R.string.aura_n_play),
            intents.playPause
        )
        builder.addAction(R.drawable.ic_aura_skip_forward, context.getString(R.string.aura_n_next), intents.next)
        intents.close?.let {
            builder.addAction(R.drawable.ic_aura_close, context.getString(R.string.aura_n_close), it)
        }

        val style = DecoratedMediaCustomViewStyle().setShowActionsInCompactView(0, 1, 2)
        if (token != null) style.setMediaSession(token)

        return builder
            .setSmallIcon(R.drawable.ic_aura_notification)
            .setStyle(style)
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setLargeIcon(cover)
            .setColorized(false)
            .setShowWhen(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    // ---------------------------------------------------------------- views

    private fun bindCard(
        views: RemoteViews,
        context: Context,
        state: State,
        cover: Bitmap?,
        density: Float,
        heightDp: Int,
        artDp: Int,
        artRadiusDp: Int,
    ) {
        views.setTextViewText(R.id.aura_n_title, state.title)
        views.setTextViewText(R.id.aura_n_artist, state.artist)

        // Width is capped so the bitmap stays small enough for a notification.
        val width = min((360 * density).toInt(), 1080)
        val height = (heightDp * density).toInt()
        views.setImageViewBitmap(
            R.id.aura_n_bg,
            renderGlass(cover, width, height, 16 * density, density)
        )

        if (cover != null) {
            val size = (artDp * density).toInt()
            views.setImageViewBitmap(R.id.aura_n_art, roundedSquare(cover, size, artRadiusDp * density))
        } else {
            views.setImageViewResource(R.id.aura_n_art, R.drawable.ic_aura_art_placeholder)
        }
    }

    private fun bindProgress(views: RemoteViews, state: State) {
        if (state.durationMs <= 0L) {
            views.setViewVisibility(R.id.aura_n_progress, View.GONE)
            views.setViewVisibility(R.id.aura_n_times, View.GONE)
            return
        }
        val max = state.durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val pos = state.positionMs.coerceIn(0L, max.toLong()).toInt()
        views.setViewVisibility(R.id.aura_n_progress, View.VISIBLE)
        views.setViewVisibility(R.id.aura_n_times, View.VISIBLE)
        views.setProgressBar(R.id.aura_n_progress, max, pos, false)
        views.setTextViewText(R.id.aura_n_position, formatTime(pos.toLong()))
        views.setTextViewText(R.id.aura_n_duration, formatTime(max.toLong()))
    }

    private fun formatTime(ms: Long): String {
        val total = ms / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    // -------------------------------------------------------------- bitmaps

    /** Glass card: blurred cover, scrim, sheen, dots, edge highlight. */
    private fun renderGlass(cover: Bitmap?, w: Int, h: Int, radius: Float, density: Float): Bitmap {
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        canvas.clipPath(Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) })

        canvas.drawColor(0xFF050505.toInt())

        if (cover != null) {
            // Cheap blur: crop to the card ratio, shrink hard, then enlarge with filtering in two steps.
            val tinyW = 24
            val tinyH = max(2, (tinyW * h.toFloat() / w).toInt())
            val tiny = Bitmap.createBitmap(tinyW, tinyH, Bitmap.Config.ARGB_8888)
            val filter = Paint(Paint.FILTER_BITMAP_FLAG)
            Canvas(tiny).drawBitmap(cover, centerCrop(cover.width, cover.height, w, h), RectF(0f, 0f, tinyW.toFloat(), tinyH.toFloat()), filter)
            val mid = Bitmap.createScaledBitmap(tiny, 96, max(2, 96 * h / w), true)
            canvas.drawBitmap(mid, null, rect, filter)
            tiny.recycle()
            mid.recycle()
        }

        canvas.drawColor(0x59000000) // dark scrim keeps text readable on any cover

        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.shader = LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(),
            intArrayOf(0x2EFFFFFF, 0x0DFFFFFF), null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(rect, fill)

        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x1AFFFFFF }
        val step = 5f * density
        var y = step / 2
        while (y < h) {
            var x = step / 2
            while (x < w) {
                canvas.drawCircle(x, y, 0.6f * density, dot)
                x += step
            }
            y += step
        }

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = density
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                intArrayOf(0x8CFFFFFF.toInt(), 0x0FFFFFFF, 0x38FFFFFF),
                null, Shader.TileMode.CLAMP
            )
        }
        val half = density / 2
        canvas.drawRoundRect(
            RectF(half, half, w - half, h - half), radius - half, radius - half, stroke
        )
        return out
    }

    private fun roundedSquare(src: Bitmap, size: Int, radius: Float): Bitmap {
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.clipPath(Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) })
        canvas.drawBitmap(src, centerCrop(src.width, src.height, size, size), rect, Paint(Paint.FILTER_BITMAP_FLAG))
        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(1f, size / 84f)
            color = 0x33FFFFFF
        }
        canvas.drawRoundRect(rect, radius, radius, edge)
        return out
    }

    private fun centerCrop(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val srcRatio = srcW.toFloat() / srcH
        val dstRatio = dstW.toFloat() / dstH
        return if (srcRatio > dstRatio) {
            val w = (srcH * dstRatio).toInt()
            val left = (srcW - w) / 2
            Rect(left, 0, left + w, srcH)
        } else {
            val h = (srcW / dstRatio).toInt()
            val top = (srcH - h) / 2
            Rect(0, top, srcW, top + h)
        }
    }

    /** Hardware bitmaps cannot be drawn onto a software canvas. */
    private fun softwareCopy(bitmap: Bitmap): Bitmap {
        val hardware = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE
        return if (hardware) bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap else bitmap
    }
}
