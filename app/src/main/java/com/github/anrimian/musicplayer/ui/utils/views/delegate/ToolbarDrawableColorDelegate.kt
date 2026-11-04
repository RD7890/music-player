package com.github.anrimian.musicplayer.ui.utils.views.delegate

import android.animation.ArgbEvaluator
import android.content.Context
import androidx.annotation.AttrRes
import com.github.anrimian.musicplayer.ui.common.isWhiteContrast
import com.github.anrimian.musicplayer.ui.common.toolbar.ToolbarBackgroundDrawable
import com.github.anrimian.musicplayer.ui.utils.attrColor

/**
 * Created on 21.01.2018.
 */
class ToolbarDrawableColorDelegate(
    context: Context,
    private val drawable: ToolbarBackgroundDrawable,
    @AttrRes startColorAttr: Int,
    @AttrRes endColorAttr: Int
) : SlideDelegate {

    private val startColor = context.attrColor(startColorAttr)
    private val endColor = context.attrColor(endColorAttr)

    private val argbEvaluator = ArgbEvaluator()

    override fun onSlide(slideOffset: Float) {
        val resultColor = argbEvaluator.evaluate(slideOffset, startColor, endColor) as Int
        drawable.setColor(resultColor, isWhiteContrast(endColor))
    }

}