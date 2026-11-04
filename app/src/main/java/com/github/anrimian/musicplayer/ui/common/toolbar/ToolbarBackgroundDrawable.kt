package com.github.anrimian.musicplayer.ui.common.toolbar

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import com.github.anrimian.musicplayer.ui.common.isWhiteContrast

class ToolbarBackgroundDrawable(
    @ColorInt backgroundColor: Int,
    @ColorInt statusBarColor: Int,
): Drawable() {

    private val bgPath = Path()
    private val corners = FloatArray(8) { 0f }
    private val bgPaint = Paint()

    private var drawStatusBarShadow = false
    private var statusBarHeight = 0
    private val statusBarRect = Rect()
    private val statusBarPaint = Paint()

    init {
        setColor(backgroundColor)
        setStatusBarColor(statusBarColor)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        updateBgPath()
        updateStatusBarRect(statusBarHeight)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(bgPath, bgPaint)
        if (drawStatusBarShadow) {
            canvas.drawRect(statusBarRect, statusBarPaint)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    override fun getOutline(outline: Outline) {
        outline.setRect(bounds)
        outline.alpha = alpha / 255.0f
    }

    fun setColor(@ColorInt color: Int, drawStatusBar: Boolean = true) {
        bgPaint.color = color
        drawStatusBarShadow = drawStatusBar
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && isWhiteContrast(color)
        invalidateSelf()
    }

    fun setStatusBarColor(@ColorInt color: Int) {
        statusBarPaint.color = color
        invalidateSelf()
    }

    fun setStatusBarHeight(height: Int) {
        statusBarHeight = height
        updateStatusBarRect(height)
        if (drawStatusBarShadow) {
            invalidateSelf()
        }
    }

    fun setCornerRadius(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float,
    ) {
        corners[0] = topLeft
        corners[1] = topLeft
        corners[2] = topRight
        corners[3] = topRight
        corners[4] = bottomRight
        corners[5] = bottomRight
        corners[6] = bottomLeft
        corners[7] = bottomLeft

        updateBgPath()
        invalidateSelf()
    }

    private fun updateStatusBarRect(height: Int) {
        statusBarRect.set(bounds.left, bounds.top, bounds.right, bounds.top + height)
    }

    private fun updateBgPath() {
        bgPath.reset()
        bgPath.addRoundRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            corners,
            Path.Direction.CW
        )
    }

}