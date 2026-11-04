package com.github.anrimian.musicplayer.ui.utils.views.coordinator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.ui.common.fab.AppFloatingActionButton;

/**
 * Created on 25.02.2018.
 */

public class ScrollAwareFABBehavior extends  CoordinatorLayout.Behavior<AppFloatingActionButton> {

    private static final int TRANSLATE_HIDE_DURATION_MILLIS = 200;
    private static final int TRANSLATE_SHOW_DURATION_MILLIS = 170;

    private ValueAnimator hideAnimator;
    private ValueAnimator showAnimator;



    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull AppFloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int axes,
                                       int type) {
        boolean verticalAxes = axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        //handle android 12 overscroll animation, if we always return true - it wouldn't work
        if (verticalAxes && directTargetChild instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) directTargetChild;
            if (!recyclerView.canScrollVertically(1) || !recyclerView.canScrollVertically(-1)) {
                return false;
            }
        }
        return verticalAxes;
    }

    //to return fab push by snackbar - uncomment and finish
    /*@SuppressLint("RestrictedApi")
    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull AppFloatingActionButton child, @NonNull View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, @NonNull AppFloatingActionButton child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            Log.d("KEK", "onDependentViewChanged: " + translationY);
            child.setCustomTranslationY(translationY);
            return true;
        }
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onDependentViewRemoved(@NonNull CoordinatorLayout parent,
                                       @NonNull AppFloatingActionButton fab,
                                       @NonNull View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            ValueAnimator anim = ValueAnimator.ofFloat(fab.getTranslationY(), 0);
            anim.addUpdateListener(animation -> {
                float translationY = (Float) animation.getAnimatedValue();
                fab.setCustomTranslationY(translationY);
            });
            anim.start();
            return;
        }
        super.onDependentViewRemoved(parent, fab, dependency);
    }*/

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull AppFloatingActionButton fab,
                               @NonNull View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed,
                               int type,
                               @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type, consumed);
            if (dyConsumed > 0 && hideAnimator == null) {
                if (showAnimator != null ) {
                    showAnimator.cancel();
                }

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                float translation = fab.getHeight() + params.bottomMargin;
                hideAnimator = ValueAnimator.ofFloat(fab.getTranslationY(), translation);
                hideAnimator.setDuration(TRANSLATE_HIDE_DURATION_MILLIS);
                hideAnimator.setInterpolator(new AccelerateInterpolator());
                hideAnimator.addUpdateListener(animation -> {
                    float translationY = (Float) animation.getAnimatedValue();
                    fab.setCustomTranslationY(translationY);
                });
                hideAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        hideAnimator = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        hideAnimator = null;
                    }
                });
                hideAnimator.start();
            } else if (dyConsumed < 0 && showAnimator == null) {
                if (hideAnimator != null) {
                    hideAnimator.cancel();
                }

                showAnimator = ValueAnimator.ofFloat(fab.getTranslationY(), 0);
                showAnimator.setDuration(TRANSLATE_SHOW_DURATION_MILLIS);
                showAnimator.setInterpolator(new DecelerateInterpolator());
                showAnimator.addUpdateListener(animation -> {
                    float translationY = (Float) animation.getAnimatedValue();
                    fab.setCustomTranslationY(translationY);
                });
                showAnimator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        showAnimator = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        showAnimator = null;
                    }
                });
                showAnimator.start();
            }
    }
}
