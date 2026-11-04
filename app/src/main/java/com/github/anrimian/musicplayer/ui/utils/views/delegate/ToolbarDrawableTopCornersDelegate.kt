package com.github.anrimian.musicplayer.ui.utils.views.delegate

import com.github.anrimian.musicplayer.ui.common.toolbar.ToolbarBackgroundDrawable

/**
 * Created on 21.01.2018.
 */
class ToolbarDrawableTopCornersDelegate(
    private val drawable: ToolbarBackgroundDrawable,
    private val expandedCornerSize: Float,
) : SlideDelegate {

    override fun onSlide(slideOffset: Float) {
        val size = expandedCornerSize * slideOffset
        drawable.setCornerRadius(size, size, 0f, 0f)
    }

}