package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers

import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentStackListener

class NavigationDrawerWrapper(private val drawer: DrawerLayout) {

    private var openedBottomSheet = false
    private var inRoot = true
    private var isInSearchMode = false
    private var isInSelectionMode = false

    private val stackChangeListener = FragmentStackListener(this::onFragmentStackChanged)

    private lateinit var navigation: FragmentNavigation

    fun setupWithNavigation(navigation: FragmentNavigation) {
        this.navigation = navigation
        onFragmentStackChanged(navigation.screensCount)
        navigation.addStackChangeListener(stackChangeListener)
    }

    fun release() {
        navigation.removeStackChangeListener(stackChangeListener)
    }

    fun onSearchModeChanged(isInSearchMode: Boolean) {
        this.isInSearchMode = isInSearchMode
        updateDrawerLockState()
    }

    fun onBottomSheetOpened(openedBottomSheet: Boolean) {
        this.openedBottomSheet = openedBottomSheet
        updateDrawerLockState()
    }

    fun onSelectionModeChanged(isInSelection: Boolean) {
        this.isInSelectionMode = isInSelection
        updateDrawerLockState()
    }

    private fun setOnRootNavigationState(inRoot: Boolean) {
        this.inRoot = inRoot
        updateDrawerLockState()
    }

    private fun onFragmentStackChanged(stackSize: Int) {
        setOnRootNavigationState(stackSize <= 1)
    }

    private fun updateDrawerLockState() {
        val lock = openedBottomSheet || !inRoot || isInSearchMode || isInSelectionMode
        drawer.setDrawerLockMode(if (lock) LOCK_MODE_LOCKED_CLOSED else LOCK_MODE_UNLOCKED)
    }

}