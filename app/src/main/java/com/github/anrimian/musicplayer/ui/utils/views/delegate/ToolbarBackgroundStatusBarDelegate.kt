package com.github.anrimian.musicplayer.ui.utils.views.delegate

import com.github.anrimian.musicplayer.ui.common.toolbar.ToolbarBackgroundDrawable

class ToolbarBackgroundStatusBarDelegate(
    private val drawable: ToolbarBackgroundDrawable,
    private val statusBarHeight: () -> Float,
): SlideDelegate {

    override fun onSlide(slideOffset: Float) {
        val resultHeight = (statusBarHeight() * slideOffset).toInt()
        drawable.setStatusBarHeight(resultHeight)
    }

}