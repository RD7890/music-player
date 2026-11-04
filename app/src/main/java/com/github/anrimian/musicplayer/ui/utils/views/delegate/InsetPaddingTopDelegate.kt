package com.github.anrimian.musicplayer.ui.utils.views.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

class InsetPaddingTopDelegate(
    view: View,
    private val startPadding: Float,
    private val endPadding: () -> Float,
): ViewSlideDelegate<View>(view) {

    private var viewHeight: Int? = null

    override fun applySlide(view: View, slideOffset: Float) {
        val deltaPadding: Float = endPadding() - startPadding
        val resultPadding: Int = (startPadding + deltaPadding * slideOffset).toInt()
        view.updatePadding(top = resultPadding)

        view.updateLayoutParams<ViewGroup.LayoutParams> {
            if (viewHeight == null) {
                viewHeight = height
            }
            height = viewHeight!! + resultPadding
        }
    }

}