package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static android.view.View.INVISIBLE;

import android.view.View;

/**
 * Created on 21.01.2018.
 */

public class InvisibilityDelegate implements SlideDelegate {

    private final View view;

    public InvisibilityDelegate(View view) {
        this.view = view;
    }

    @Override
    public void onSlide(float slideOffset) {
        view.setAlpha(0f);
        view.setVisibility(INVISIBLE);
    }
}
