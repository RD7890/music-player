package com.github.anrimian.musicplayer.ui.utils.fragments.navigation

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.LinkedList

fun FragmentNavigation.attachSimpleBackPressedCallback(
    activity: FragmentActivity,
): OnBackPressedCallback {
    val onBackPressedCallback = object : OnBackPressedCallback(screensCount > 1) {
        override fun handleOnBackPressed() {
            goBack()
        }
    }
    activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
    addStackChangeListener { stackSize -> onBackPressedCallback.isEnabled = stackSize > 1 }
    return onBackPressedCallback
}

fun FragmentNavigation.attachBackPressedCallback(
    activity: FragmentActivity,
): BackActionRemoveCallback {
    val tag = "navigation_back_manager_fragment_tag"
    val fm = activity.supportFragmentManager
    var container = fm.findFragmentByTag(tag) as NavigationBackManagerFragment?
    if (container == null) {
        container = NavigationBackManagerFragment()
        fm.beginTransaction()
            .add(container, tag)
            .commitAllowingStateLoss()
    }
    val backManager = container.getFragmentNavigation()

    if (backManager.getAttachedNavigationCount() == 0) {
        val onBackPressedCallback = object : OnBackPressedCallback(backManager.getTotalScreensCount() > 1) {
            override fun handleOnBackPressed() {
                backManager.dispatchBack()
            }
        }
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        backManager.addTotalScreensCountChangeListener { screensCount -> onBackPressedCallback.isEnabled = screensCount > 1 }
    }

    backManager.attachFragmentNavigation(this)

    return BackActionRemoveCallback(backManager, this)
}

class BackActionRemoveCallback(
    private val backManager: FragmentNavigationSetBackManager,
    private val fragmentNavigation: FragmentNavigation
) {

    fun setPaused(isPaused: Boolean) {
        backManager.pauseFragmentNavigation(fragmentNavigation, isPaused)
    }

    fun remove() {
        backManager.detachFragmentNavigation(fragmentNavigation)
    }

}

class FragmentNavigationSetBackManager {

    private val navigationList = LinkedList<FragmentNavigation>()
    private val pausedNavigationMap = HashSet<FragmentNavigation>()
    private val totalScreenCountChangeListeners = LinkedList<((Int) -> Unit)>()

    fun attachFragmentNavigation(fragmentNavigation: FragmentNavigation) {
        navigationList.add(fragmentNavigation)
        totalScreenCountChangeListeners.forEach { listener -> listener(getTotalScreensCount()) }

        val stackListener = FragmentStackListener { _ ->
            totalScreenCountChangeListeners.forEach { listener -> listener(getTotalScreensCount()) }
        }
        fragmentNavigation.addStackChangeListener(stackListener)
    }

    fun pauseFragmentNavigation(fragmentNavigation: FragmentNavigation, isPaused: Boolean) {
        if (isPaused) {
            pausedNavigationMap.add(fragmentNavigation)
        } else {
            pausedNavigationMap.remove(fragmentNavigation)
        }
    }

    fun detachFragmentNavigation(fragmentNavigation: FragmentNavigation) {
        navigationList.remove(fragmentNavigation)
        totalScreenCountChangeListeners.forEach { listener -> listener(getTotalScreensCount()) }
    }

    fun getAttachedNavigationCount() = navigationList.size

    fun getTotalScreensCount(): Int {
        var totalScreensCount = 0
        navigationList.forEach { navigation -> totalScreensCount += navigation.screensCount }
        // in case of nested navigation remove their first screens from calculations
        totalScreensCount -= navigationList.size - 1
        return totalScreensCount
    }

    fun addTotalScreensCountChangeListener(listener: (Int) -> Unit) {
        totalScreenCountChangeListeners.add(listener)
    }

    fun dispatchBack() {
        val iterator = navigationList.listIterator(navigationList.size)
        while (iterator.hasPrevious()) {
            val navigation = iterator.previous()
            if (!pausedNavigationMap.contains(navigation)) {
                navigation.goBack()
                return
            }
        }
    }

}

class NavigationBackManagerFragment : Fragment() {

    private val navigationManager by lazy { FragmentNavigationSetBackManager() }

    fun getFragmentNavigation() = navigationManager

}