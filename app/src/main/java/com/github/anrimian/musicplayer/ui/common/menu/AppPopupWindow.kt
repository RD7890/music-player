package com.github.anrimian.musicplayer.ui.common.menu

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.getDrawableCompat

@SuppressLint("RtlHardcoded")
object AppPopupWindow {

    private const val POPUP_OPEN_WINDOW_MILLIS = 200L
    private var lastOpenTime: Long = 0

    /**
     * @param anchorGravity values: TOP, BOTTOM, START, END, CENTER
     * @param gravity values: NO_GRAVITY, TOP, BOTTOM, CENTER_VERTICAL, START, END,
     * CENTER_HORIZONTAL
     */
    fun showPopupWindow(
        anchorView: View,
        popupView: View,
        screenMargin: Int,
        anchorGravity: Int,
        gravity: Int = Gravity.NO_GRAVITY
    ): PopupWindow? {
        val currentTime = System.currentTimeMillis()
        if (lastOpenTime + POPUP_OPEN_WINDOW_MILLIS > currentTime) {
            return null
        }
        lastOpenTime = currentTime

        val context = anchorView.context

        //margins
        val popupViewWrapper = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(screenMargin, screenMargin, screenMargin, screenMargin)
        popupViewWrapper.addView(popupView, params)

        popupView.elevation = 5f

        val popupWindow = PopupWindow(
            popupViewWrapper,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.animationStyle = R.style.PopupAnimationStyle
        //fix for closing by back button or touch on android 5.1
        popupWindow.setBackgroundDrawable(context.getDrawableCompat(R.drawable.bg_transparent))
        //fix for closing by touch on android 12
        popupWindow.isOutsideTouchable = true

        popupView.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )


        val location = IntArray(2)
        anchorView.getLocationInWindow(location)
        val anchorX = location[0]
        val anchorY = location[1]

        val anchorWidth = anchorView.measuredWidth
        val anchorHeight = anchorView.measuredHeight

        val viewWidth = popupView.measuredWidth
        val viewHeight = popupView.measuredHeight

        var showX = anchorX
        var showY = anchorY

        val anchorGravityNormalized = normalizeGravity(anchorView, anchorGravity)
        val gravityNormalized = normalizeGravity(anchorView, gravity)
        when (anchorGravityNormalized) {
            Gravity.TOP -> showY -= viewHeight - screenMargin
            Gravity.BOTTOM -> showY += anchorHeight - screenMargin
            Gravity.LEFT -> showX = showX - screenMargin - viewWidth
            Gravity.RIGHT -> showX += anchorWidth
            Gravity.CENTER -> showX = showX + anchorWidth - viewWidth
        }
        when (gravityNormalized) {
            Gravity.NO_GRAVITY -> {
                when (anchorGravityNormalized) {
                    Gravity.LEFT, Gravity.RIGHT -> showY -= screenMargin
                    Gravity.TOP, Gravity.BOTTOM -> showX -= screenMargin
                    Gravity.CENTER -> {
                        showY -= screenMargin
                        showX -= screenMargin
                    }
                }
            }
            Gravity.TOP -> showY = showY + anchorHeight - viewHeight - screenMargin
            Gravity.BOTTOM -> showY += anchorHeight
            Gravity.CENTER_VERTICAL -> {
                showY = showY + anchorHeight / 2 - viewHeight / 2 - screenMargin
            }
            Gravity.LEFT -> showX -= viewWidth
            Gravity.RIGHT -> showX += viewWidth
            Gravity.CENTER_HORIZONTAL -> showX = showX + anchorWidth / 2 - viewWidth / 2
        }

        val listener = object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {}

            override fun onViewDetachedFromWindow(view: View) {
                popupWindow.dismiss()
            }
        }
        anchorView.addOnAttachStateChangeListener(listener)
        popupWindow.setOnDismissListener { anchorView.removeOnAttachStateChangeListener(listener) }

        try {
            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, showX, showY)
        } catch (ignored: IllegalStateException) {
            return null
        }

        return popupWindow
    }

    private fun normalizeGravity(view: View, gravity: Int): Int {
        val isRtl = ViewUtils.isRtl(view)
        if (gravity == Gravity.START) {
            return if (isRtl) {
                Gravity.RIGHT
            } else {
                Gravity.LEFT
            }
        }
        if (gravity == Gravity.END) {
            return if (isRtl) {
                Gravity.LEFT
            } else {
                Gravity.RIGHT
            }
        }
        return gravity
    }
}
