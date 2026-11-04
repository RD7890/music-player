package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

import androidx.constraintlayout.motion.widget.MotionLayout;

/**
 * Created on 14.01.2018.
 * Call onSlide should be bound between 0.008f and 0.95f. Otherwise MotionLayout will misbehave
 */
public class MotionLayoutDelegate implements SlideDelegate {

    private final MotionLayout motionLayout;

    private int transitionId;

    public MotionLayoutDelegate(MotionLayout motionLayout) {
       this.motionLayout = motionLayout;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(motionLayout)) {
            if (motionLayout.getVisibility() != VISIBLE) {
                motionLayout.setVisibility(VISIBLE);
            }
            moveView(slideOffset);
        } else {
            motionLayout.post(() -> {
                animateVisibility(motionLayout, VISIBLE);
                moveView(slideOffset);
            });
        }
    }

    public void setTransitionId(int transitionId) {
        if (this.transitionId != transitionId) {
            this.transitionId = transitionId;
            motionLayout.setTransition(transitionId);
        }
    }

    private void moveView(float slideOffset) {
        motionLayout.setProgress(slideOffset);
    }
}
