package com.github.anrimian.musicplayer.ui.common.fab

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AppFloatingActionButton : FloatingActionButton {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    //consume calls to prevent unnecessary move from snackbar
    override fun setTranslationY(translationY: Float) {}

    fun setCustomTranslationY(translationY: Float) {
        super.setTranslationY(translationY)
    }

}