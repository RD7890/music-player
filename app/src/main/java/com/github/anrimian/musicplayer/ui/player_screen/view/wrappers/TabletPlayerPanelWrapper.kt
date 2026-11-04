package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers

import android.view.View
import com.github.anrimian.musicplayer.ui.utils.applyBottomHeightInsets

class TabletPlayerPanelWrapper(
    panelView: View,
    toolbarNavigationWrapper: ToolbarNavigationWrapper,
    bottomSheetStateListener: (Boolean) -> Unit
) : PlayerPanelWrapper() {

    init {
        bottomSheetStateListener(false)
        toolbarNavigationWrapper.onBottomSheetStateChanged(false)

        panelView.applyBottomHeightInsets()
    }

    override fun isBottomPanelExpanded(): Boolean = false

    override fun collapseBottomPanelSmoothly() {}

    override fun collapseBottomPanelSmoothly(doOnCollapse: () -> Unit) {
        doOnCollapse.invoke()
    }

    override fun collapseBottomPanel() {}

    override fun expandBottomPanel(jumpToState: Boolean) {}

}
