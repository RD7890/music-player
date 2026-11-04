package com.github.anrimian.musicplayer.ui.utils.views.delegate

import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable

/**
 * Created on 21.01.2018.
 */
class ItemDrawableTopCornersDelegate(
    private val drawable: ItemDrawable,
    private val expandedCornerSize: Float,
) : SlideDelegate {

    override fun onSlide(slideOffset: Float) {
        val size = expandedCornerSize * slideOffset
        drawable.setCornerRadius(size, size, 0f, 0f)
    }

}