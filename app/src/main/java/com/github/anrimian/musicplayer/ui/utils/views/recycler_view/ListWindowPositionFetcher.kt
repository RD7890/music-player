package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager

class ListWindowPositionFetcher(private val layoutManager: LinearLayoutManager) {

    private val handler = Handler(Looper.getMainLooper())
    private var repeatCount = 0

    fun requestWindowPositions(result: (first: Int, last: Int) -> Unit) {
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        if (firstVisiblePosition == -1 || lastVisiblePosition == -1) {
            if (repeatCount > 10) {
                repeatCount = 0
                result(firstVisiblePosition, lastVisiblePosition)
                return
            }
            repeatCount++
            handler.postDelayed({ requestWindowPositions(result) }, 50)
        } else {
            result(firstVisiblePosition, lastVisiblePosition)
        }
    }

}