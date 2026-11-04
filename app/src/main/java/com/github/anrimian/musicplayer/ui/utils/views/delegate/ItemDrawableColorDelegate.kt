package com.github.anrimian.musicplayer.ui.utils.views.delegate

import android.animation.ArgbEvaluator
import android.content.Context
import androidx.annotation.AttrRes
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable

/**
 * Created on 21.01.2018.
 */
class ItemDrawableColorDelegate(
    context: Context,
    private val drawable: ItemDrawable,
    @AttrRes startColorAttr: Int,
    @AttrRes endColorAttr: Int
) : SlideDelegate {

    private val startColor = context.attrColor(startColorAttr)
    private val endColor = context.attrColor(endColorAttr)

    private val argbEvaluator = ArgbEvaluator()

    override fun onSlide(slideOffset: Float) {
        val resultColor = argbEvaluator.evaluate(slideOffset, startColor, endColor) as Int
        drawable.setColor(resultColor)
    }

}