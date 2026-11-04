package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers // Or a more common ui.common.toolbar package

import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentStackListener

class ToolbarNavigationWrapper(
    private val toolbar: AdvancedToolbar,
    private val navigation: FragmentNavigation,
    private var isBottomSheetExpanded: Boolean,
    onNavigationIconClick: () -> Unit
) {

    private val stackChangeListener = FragmentStackListener {
        updateToolbarState(animate = true)
    }

    init {
        toolbar.setNavigationButtonClickListener(onNavigationIconClick)
        navigation.addStackChangeListener(stackChangeListener)
        updateToolbarState(animate = false)
    }

    fun onBottomSheetStateChanged(isExpanded: Boolean) {
        this.isBottomSheetExpanded = isExpanded
        updateToolbarState(animate = false)
    }

    fun onBottomSheetSlided(slideOffset: Float) {
        if (isInitialized() && isAtRootScreen() && !toolbar.isInSearchMode() && !toolbar.isInActionMode()) {
            toolbar.setNavigationButtonProgress(slideOffset)
        }
    }

    fun release() {
        navigation.removeStackChangeListener(stackChangeListener)
    }

    private fun updateToolbarState(animate: Boolean) {
        if (isInitialized()) {
            toolbar.setNavigationButtonMode(
                isBase = isAtRootScreen() && !isBottomSheetExpanded && !toolbar.isInSearchMode() && !toolbar.isInActionMode(),
                animate = animate
            )
        }

        toolbar.setNavigationButtonBackModeLocked(!isAtRootScreen() || isBottomSheetExpanded)
    }

    private fun isAtRootScreen() = navigation.screensCount <= 1

    private fun isInitialized() = navigation.screensCount > 0
}