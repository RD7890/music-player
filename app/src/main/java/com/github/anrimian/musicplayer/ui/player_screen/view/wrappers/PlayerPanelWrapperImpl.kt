package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentPlayerBinding
import com.github.anrimian.musicplayer.ui.player_screen.view.slide.ToolbarDelegate
import com.github.anrimian.musicplayer.ui.player_screen.view.slide.ToolbarVisibilityDelegate
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager
import com.github.anrimian.musicplayer.ui.utils.views.delegate.LeftBottomShadowDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MoveXDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MoveYDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PlayerPanelWrapperImpl(
    private val binding: FragmentPlayerBinding,
    private val activity: FragmentActivity,
    private val toolbarNavigationWrapper: ToolbarNavigationWrapper,
    savedInstanceState: Bundle?,
    private val onBottomSheetDragCollapsed: () -> Unit,
    private val onBottomSheetDragExpanded: () -> Unit,
    private val bottomSheetStateListener: (Boolean) -> Unit,
) : PlayerPanelWrapper() {

    private val bottomSheetBehavior: BottomSheetBehavior<View>
    private val bottomSheetDelegate: SlideDelegate

    private val onBackPressedCallback: OnBackPressedCallback

    private var collapseDelayedAction: (() -> Unit)? = null

    init {
        // set view start state for smooth initialization
        binding.toolbarPlayQueue.alpha = 0f // required
        if (savedInstanceState == null) {
            binding.controlPanelView.visibility = View.INVISIBLE
            // prevents flicker on start if panel is collapsed
            binding.clPlayerPagerContainer.visibility = View.INVISIBLE
        }

        binding.controlPanelView.isClickable = true

        bottomSheetDelegate = createBottomSheetDelegate()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.controlPanelView)
        bottomSheetBehavior.addBottomSheetCallback(SimpleBottomSheetCallback(
            this::onBottomSheetStateChanged,
            bottomSheetDelegate::onSlide
        ))

        onBackPressedCallback = object : OnBackPressedCallback(isBottomPanelExpanded()) {
            override fun handleOnBackPressed() {
                collapseBottomPanelSmoothly()
            }
        }
        activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            //on low versions motion layout doesn't call insets listener
            binding.clDrawerContent.applyBottomInsets()
        } else {
            binding.controlPanelView.applyBottomInsets(activity.resources.getDimensionPixelSize(R.dimen.bottom_sheet_expand_height))
        }
    }

    override fun isBottomPanelExpanded(): Boolean {
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    override fun collapseBottomPanel() {
        bottomSheetStateListener(false)

        binding.controlPanelView.setPlayButtonsSelectableBackground(
            AndroidUtils.getResourceIdFromAttr(activity, R.attr.selectableItemBackgroundBorderless)
        )

        bottomSheetDelegate.onSlide(0f)
        onBackPressedCallback.isEnabled = false
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun collapseBottomPanelSmoothly() {
        bottomSheetStateListener(false)

        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            if (collapseDelayedAction != null) {
                collapseDelayedAction!!.invoke()
                collapseDelayedAction = null
            }
        }
    }

    override fun collapseBottomPanelSmoothly(doOnCollapse: () -> Unit) {
        collapseDelayedAction = doOnCollapse
        collapseBottomPanelSmoothly()
    }

    override fun expandBottomPanel(jumpToState: Boolean) {
        binding.controlPanelView.setPlayButtonsSelectableBackground(R.drawable.bg_selectable_round_shape)
        onBackPressedCallback.isEnabled = true
        if (jumpToState) {
            bottomSheetStateListener(true)

            bottomSheetDelegate.onSlide(1f)
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        } else {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun onBottomSheetStateChanged(newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_COLLAPSED -> {
                if (collapseDelayedAction != null) {
                    collapseDelayedAction!!.invoke()
                    collapseDelayedAction = null
                }
                onBottomSheetDragCollapsed.invoke()
                onBackPressedCallback.isEnabled = false
                toolbarNavigationWrapper.onBottomSheetStateChanged(false)
                return
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                onBottomSheetDragExpanded.invoke()
                onBackPressedCallback.isEnabled = true
                toolbarNavigationWrapper.onBottomSheetStateChanged(true)
            }
        }
    }

    private fun createBottomSheetDelegate(): SlideDelegate {
        val boundDelegateManager = DelegateManager().apply {
            addDelegate(BoundValuesDelegate(0.4f, 1f, VisibilityDelegate(binding.toolbarPlayQueue)))
            addDelegate(ReverseDelegate(BoundValuesDelegate(0.0f, 0.8f, ToolbarVisibilityDelegate(binding.toolbar))))
            addDelegate(BoundValuesDelegate(0f, 0.6f, ReverseDelegate(VisibilityDelegate(binding.toolbar.getToolbarModesViewGroup()))))
            addDelegate(BoundValuesDelegate(0.7f, 0.95f, ReverseDelegate(VisibilityDelegate(binding.drawerFragmentContainer))))
            addDelegate(ToolbarDelegate(binding.toolbar, toolbarNavigationWrapper))
            addDelegate(binding.controlPanelView.getPanelCollapseStateDelegate())
        }

        val delegateManager = DelegateManager()
        if (isInLandscapeOrientation()) { //landscape
            boundDelegateManager.addDelegate(MoveXDelegate(0.5f, binding.clBottomSheetContainer))
            boundDelegateManager.addDelegate(LeftBottomShadowDelegate(
                    binding.bottomSheetLeftShadow,
                    binding.bottomSheetTopLeftShadow,
                    binding.controlPanelView,
                    binding.clBottomSheetContainer,
                    binding.toolbar))
            delegateManager.addDelegate(MoveYDelegate(binding.clPlayerPagerContainer, 0.85f, activity.resources.getDimensionPixelSize(R.dimen.bottom_sheet_height)))
            boundDelegateManager.addDelegate(BoundValuesDelegate(0f, 0.1f, VisibilityDelegate(binding.clPlayerPagerContainer)))
        } else {
            boundDelegateManager.addDelegate(BoundValuesDelegate(0.90f, 1f, VisibilityDelegate(binding.clPlayerPagerContainer)))
            delegateManager.addDelegate(MoveYDelegate(binding.clPlayerPagerContainer, 0.3f))
        }
        delegateManager.addDelegate(BoundValuesDelegate(0.008f, 0.95f, boundDelegateManager))
        return delegateManager
    }

    private fun isInLandscapeOrientation() = binding.bottomSheetTopLeftShadow != null

}
