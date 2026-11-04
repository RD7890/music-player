package com.github.anrimian.musicplayer.ui.utils.views.delegate

import android.view.View

class ElevationDelegate(
    view: View,
    private val startElevation: Float,
    private val endElevation: Float,
): ViewSlideDelegate<View>(view) {

    override fun applySlide(view: View, slideOffset: Float) {
        val deltaElevation = endElevation - startElevation
        val resultElevation = startElevation + deltaElevation * slideOffset
        view.elevation = resultElevation
    }

}