package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static androidx.core.view.ViewCompat.isLaidOut;

import android.view.View;

/**
 * Created on 14.01.2018.
 */

public class LeftBottomShadowDelegate implements SlideDelegate {

    private final View leftShadow;
    private final View topLeftShadow;
    private final View controlPanelView;
    private final View bottomSheetCoordinator;
    private final View toolbar;

    public LeftBottomShadowDelegate(View leftShadow,
                                    View topLeftShadow,
                                    View controlPanelView,
                                    View bottomSheetCoordinator,
                                    View toolbar) {
        this.leftShadow = leftShadow;
        this.topLeftShadow = topLeftShadow;
        this.controlPanelView = controlPanelView;
        this.bottomSheetCoordinator = bottomSheetCoordinator;
        this.toolbar = toolbar;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(leftShadow)
                && isLaidOut(topLeftShadow)
                && isLaidOut(controlPanelView)
                && isLaidOut(bottomSheetCoordinator)) {
            moveView();
        } else {
            leftShadow.post(this::moveView);
        }
    }

    private void moveView() {
        float x = bottomSheetCoordinator.getX() - leftShadow.getWidth();
        leftShadow.setX(x);
        topLeftShadow.setX(x);

        float y = controlPanelView.getY() + toolbar.getBottom();
        leftShadow.setY(y);
        topLeftShadow.setY(y - topLeftShadow.getMeasuredHeight());
    }
}
