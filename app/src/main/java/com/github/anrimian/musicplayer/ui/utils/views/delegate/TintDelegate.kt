package com.github.anrimian.musicplayer.ui.utils.views.delegate

import android.animation.ArgbEvaluator
import android.widget.ImageView
import androidx.annotation.AttrRes
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr

/**
 * Created on 21.01.2018.
 */
class TintDelegate(
    view: ImageView,
    @AttrRes startColorAttr: Int,
    @AttrRes endColorAttr: Int
) : ViewSlideDelegate<ImageView>(view) {

    private val startColor = view.context.colorFromAttr(startColorAttr)
    private val endColor = view.context.colorFromAttr(endColorAttr)

    private val argbEvaluator = ArgbEvaluator()

    override fun applySlide(view: ImageView, slideOffset: Float) {
        val resultColor = argbEvaluator.evaluate(slideOffset, startColor, endColor) as Int
        view.setColorFilter(resultColor)
    }

}