package com.github.anrimian.musicplayer.ui.utils.views.delegate

import android.animation.ArgbEvaluator
import android.view.View
import androidx.annotation.AttrRes
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr

/**
 * Created on 21.01.2018.
 */
class BackgroundColorDelegate(
    view: View,
    @AttrRes startColorAttr: Int,
    @AttrRes endColorAttr: Int
) : ViewSlideDelegate<View>(view) {

    private val startColor = view.context.colorFromAttr(startColorAttr)
    private val endColor = view.context.colorFromAttr(endColorAttr)

    private val argbEvaluator = ArgbEvaluator()

    override fun applySlide(view: View, slideOffset: Float) {
        val resultColor = argbEvaluator.evaluate(slideOffset, startColor, endColor) as Int
        view.setBackgroundColor(resultColor)
    }

}