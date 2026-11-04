package com.github.anrimian.musicplayer.ui.player_screen.view.slide

import android.animation.ArgbEvaluator
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.ToolbarNavigationWrapper
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate

class ToolbarDelegate(
    private val toolbar: AdvancedToolbar,
    private val toolbarNavigationWrapper: ToolbarNavigationWrapper
) : SlideDelegate {
    
    private val startStatusBarColor = toolbar.context.attrColor(R.attr.actionModeStatusBarColor)
    private val endStatusBarColor = toolbar.context.attrColor(R.attr.actionModeStatusBarColor)

    override fun onSlide(slideOffset: Float) {
        toolbarNavigationWrapper.onBottomSheetSlided(slideOffset)

        if (toolbar.isInActionMode()) {
            val startColor = toolbar.context.attrColor(R.attr.actionModeTextColor)
            val endColor = toolbar.context.attrColor(R.attr.toolbarTextColorPrimary)
            val argbEvaluator = ArgbEvaluator()
            val color = argbEvaluator.evaluate(slideOffset, startColor, endColor) as Int
            toolbar.setControlButtonColor(color)

            val statusBarColor = argbEvaluator.evaluate(slideOffset, startStatusBarColor, endStatusBarColor) as Int
            toolbar.setStatusBarColor(statusBarColor)
        }
    }
    
}
