package com.github.anrimian.musicplayer.ui.utils.views.progress_state;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

public class ProgressStateView extends LinearLayout {

    private static final int NO_DRAWABLE = -1;
    private static final int PROGRESS_SHOW_DELAY_MILLIS = 750;

    private final Handler handler = new Handler();

    private final int verticalMargin;

    private Runnable onTryAgainClick;

    private Views views;

    public ProgressStateView(Context context) {
        this(context, null);
    }

    public ProgressStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        verticalMargin = context.getResources().getDimensionPixelSize(R.dimen.margin_small);
        int horizontalMargin = context.getResources().getDimensionPixelSize(R.dimen.margin_normal);
        setPadding(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
    }

    public void onTryAgainClick(Runnable listener) {
        onTryAgainClick = listener;
    }

    public void hideAll(Runnable onHidden) {
        if (views != null) {
            views.hideAll(onHidden);
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void hideAll() {
        if (views != null) {
            views.hideAll();
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void showMessage(@StringRes int messageId) {
        showMessage(messageId, false);
    }

    public void showMessage(@StringRes int messageId, boolean showTryAgainButton) {
        showMessage(messageId, NO_DRAWABLE, showTryAgainButton);
    }

    public void showMessage(@StringRes int messageId, @StringRes int buttonText) {
        showMessage(getContext().getString(messageId), buttonText);
    }

    public void showMessage(int messageId, @DrawableRes int emptyImageRes, boolean showTryAgainButton) {
        String message = getContext().getString(messageId);
        showMessage(message, emptyImageRes, showTryAgainButton);
    }

    public void showMessage(String message, boolean showTryAgainButton) {
        showMessage(message, NO_DRAWABLE, showTryAgainButton);
    }

    public void showMessage(String message, @StringRes int buttonText) {
        showMessage(message, getContext().getString(buttonText));
    }

    public void showMessage(String message, @Nullable String buttonText) {
        showMessage(message, NO_DRAWABLE, buttonText);
    }

    public void showMessage(String message, @DrawableRes int imageRes, boolean showTryAgainButton) {
        String buttonText = showTryAgainButton? getContext().getString(R.string.try_again) : null;
        showMessage(message, imageRes, buttonText);
    }

    public void showMessage(String message, @DrawableRes int imageRes, @Nullable String buttonText) {
        getInitializedViews().showMessage(message, imageRes, buttonText);
        handler.removeCallbacksAndMessages(null);
    }

    public void showProgress() {
        handler.postDelayed(getInitializedViews()::showProgress, PROGRESS_SHOW_DELAY_MILLIS);
    }

    private Views getInitializedViews() {
        if (views == null) {
            views = new Views();
        }
        return views;
    }

    private class Views {
        private final ProgressBar progressBar;
        private final ImageView ivEmpty;
        private final TextView tvMessage;
        private final TextView btnTryAgain;
        private final LinearLayout root = ProgressStateView.this;

        public Views() {
            Context context = getContext();

            root.setOrientation(VERTICAL);
            root.setGravity(Gravity.CENTER);
            root.setBackgroundResource(AndroidUtils.getResourceIdFromAttr(context, android.R.attr.colorBackground));

            progressBar = new ProgressBar(context);
            progressBar.setVisibility(INVISIBLE);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            pbParams.topMargin = verticalMargin;
            pbParams.bottomMargin = verticalMargin;
            root.addView(progressBar, pbParams);

            ivEmpty = new ImageView(context);
            ivEmpty.setVisibility(INVISIBLE);
            ivEmpty.setContentDescription(context.getString(R.string.useless_image));
            int imageSize = AndroidUtils.dpToPx(150, context);
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(imageSize, imageSize);
            ivParams.topMargin = verticalMargin;
            ivParams.bottomMargin = verticalMargin;
            root.addView(ivEmpty, ivParams);

            tvMessage = new TextView(context);
            tvMessage.setVisibility(INVISIBLE);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            tvParams.topMargin = verticalMargin;
            tvParams.bottomMargin = verticalMargin;
            tvMessage.setLayoutParams(tvParams);
            tvMessage.setGravity(Gravity.CENTER_HORIZONTAL);
            tvMessage.setTextColor(AndroidUtils.getColorFromAttr(context, android.R.attr.textColorPrimary));
            tvMessage.setTextSize(19f);
            root.addView(tvMessage);

            btnTryAgain = new TextView(context);
            btnTryAgain.setBackgroundResource(R.drawable.bg_button_outline);
            btnTryAgain.setTextColor(ContextCompat.getColorStateList(context, R.color.color_accent_state));
            int buttonPadding = AndroidUtils.dpToPx(16, context);
            btnTryAgain.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            btnTryAgain.setTextSize(18f);
            btnTryAgain.setVisibility(INVISIBLE);
            btnTryAgain.setGravity(Gravity.CENTER_HORIZONTAL);
            btnTryAgain.setOnClickListener(v -> {
                if (onTryAgainClick != null) {
                    onTryAgainClick.run();
                }
            });
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            btnParams.topMargin = verticalMargin;
            btnParams.bottomMargin = verticalMargin;
            root.addView(btnTryAgain, btnParams);
        }

        public void hideAll(Runnable onHidden) {
            animateVisibility(root, INVISIBLE, onHidden);
            root.setClickable(false);
        }

        public void hideAll() {
            root.setVisibility(INVISIBLE);
            root.setClickable(false);
        }

        public void showMessage(String message,
                                @DrawableRes int imageRes,
                                @Nullable String buttonText) {
            root.setVisibility(VISIBLE);
            root.setClickable(true);
            root.setContentDescription(message);
            progressBar.setVisibility(GONE);
            tvMessage.setVisibility(VISIBLE);

            if (message != null) {
                tvMessage.setText(message);
            }
            if (imageRes != NO_DRAWABLE) {
                ivEmpty.setImageResource(imageRes);
                ivEmpty.setVisibility(VISIBLE);
            } else {
                ivEmpty.setVisibility(GONE);
            }
            if (buttonText != null) {
                btnTryAgain.setText(buttonText);
                btnTryAgain.setVisibility(VISIBLE);
            } else {
                btnTryAgain.setVisibility(GONE);
            }
        }

        private void showProgress() {
            root.setContentDescription(getContext().getString(R.string.loading_progress));
            root.setVisibility(VISIBLE);
            root.setClickable(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(VISIBLE);
            tvMessage.setVisibility(GONE);
            btnTryAgain.setVisibility(GONE);
            ivEmpty.setVisibility(GONE);
        }
    }

}
