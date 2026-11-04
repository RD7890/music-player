package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers

abstract class PlayerPanelWrapper {

    abstract fun isBottomPanelExpanded(): Boolean
    abstract fun collapseBottomPanel()
    abstract fun collapseBottomPanelSmoothly()
    abstract fun collapseBottomPanelSmoothly(doOnCollapse: () -> Unit)
    abstract fun expandBottomPanel(jumpToState: Boolean)

}
