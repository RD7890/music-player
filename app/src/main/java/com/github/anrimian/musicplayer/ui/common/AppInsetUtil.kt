package com.github.anrimian.musicplayer.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.applyCoordinatorBottomMarginInsets
import com.github.anrimian.musicplayer.ui.utils.applyImeBottomPaddingInsets
import com.github.anrimian.musicplayer.ui.utils.applyImeOffsetBottomPaddingInsets
import com.github.anrimian.musicplayer.ui.utils.applyTopInsets
import com.github.anrimian.musicplayer.ui.utils.getDimensionPixelSize
import com.github.anrimian.musicplayer.ui.utils.isTabletLand

fun View.applyFabBottomInsets() {
    //on lower versions it's unnecessary by unknown reason
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        applyCoordinatorBottomMarginInsets()
    }
}

@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun View.applyDrawerHeaderInsets() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        applyTopInsets()
    } else {
        val resources = context.resources
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        updateLayoutParams<LinearLayout.LayoutParams> {
            height += statusBarHeight
        }
        updatePadding(top = paddingTop + statusBarHeight)
    }
}

fun View.applyLibraryProgressViewOffset(activity: Activity) {
    if (context.isTabletLand()) {
        applyImeBottomPaddingInsets(activity)
    } else {
        applyImeOffsetBottomPaddingInsets(activity, context.getDimensionPixelSize(R.dimen.bottom_sheet_height))
    }
}