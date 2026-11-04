package com.github.anrimian.musicplayer.ui.player_screen.view.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.PartialQueueToolbarBinding
import com.github.anrimian.musicplayer.ui.utils.onPageScrolled
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager
import com.github.anrimian.musicplayer.ui.utils.views.delegate.InvisibilityDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TransitionVisibilityDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate

class QueueToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val binding = PartialQueueToolbarBinding.inflate(LayoutInflater.from(context), this)

    private val motionLayoutDelegate = MotionLayoutDelegate(binding.mlQueueToolbar)
    private val lyricsPlayQueueSlideDelegate = DelegateManager()
    private val nowPlayingPlayQueueSlideDelegate: DelegateManager
    private val lyricsNowPlayingSlideDelegate: DelegateManager

    init {
        lyricsPlayQueueSlideDelegate.addDelegate(motionLayoutDelegate)
        lyricsPlayQueueSlideDelegate.addDelegate(
            BoundValuesDelegate(
                0.4f,
                0.9f,
                VisibilityDelegate(binding.tvQueueTitle)
            )
        )
        lyricsPlayQueueSlideDelegate.addDelegate(
            TransitionVisibilityDelegate(
                0.02f,
                0.04f,
                0.96f,
                0.98f,
                binding.transitionShadow,
            )
        )
        lyricsPlayQueueSlideDelegate.addDelegate(
            BoundValuesDelegate(
                0.85f,
                0.97f,
                VisibilityDelegate(binding.tvQueueSubtitle)
            )
        )
        lyricsPlayQueueSlideDelegate.addDelegate(
            ReverseDelegate(
                BoundValuesDelegate(
                    0.3f,
                    0.9f,
                    VisibilityDelegate(binding.tvLyricsTitle)
                )
            )
        )
        lyricsPlayQueueSlideDelegate.addDelegate(InvisibilityDelegate(binding.tvNowPlaying))

        nowPlayingPlayQueueSlideDelegate = DelegateManager()
        nowPlayingPlayQueueSlideDelegate.addDelegate(motionLayoutDelegate)
        nowPlayingPlayQueueSlideDelegate.addDelegate(
            BoundValuesDelegate(
                0.4f,
                0.9f,
                VisibilityDelegate(binding.tvQueueTitle)
            )
        )
        nowPlayingPlayQueueSlideDelegate.addDelegate(
            TransitionVisibilityDelegate(
                0.02f,
                0.04f,
                0.96f,
                0.98f,
                binding.transitionShadow,
            )
        )
        nowPlayingPlayQueueSlideDelegate.addDelegate(
            BoundValuesDelegate(
                0.85f,
                0.97f,
                VisibilityDelegate(binding.tvQueueSubtitle)
            )
        )
        nowPlayingPlayQueueSlideDelegate.addDelegate(InvisibilityDelegate(binding.tvLyricsTitle))
        nowPlayingPlayQueueSlideDelegate.addDelegate(
            ReverseDelegate(
                BoundValuesDelegate(
                    0.3f,
                    0.9f,
                    VisibilityDelegate(binding.tvNowPlaying)
                )
            )
        )

        lyricsNowPlayingSlideDelegate = DelegateManager()
        lyricsNowPlayingSlideDelegate.addDelegate(motionLayoutDelegate)
        lyricsNowPlayingSlideDelegate.addDelegate(
            ReverseDelegate(
                BoundValuesDelegate(
                    0.3f,
                    0.9f,
                    VisibilityDelegate(binding.tvLyricsTitle)
                )
            )
        )
        lyricsNowPlayingSlideDelegate.addDelegate(
            BoundValuesDelegate(
                0.4f,
                0.9f,
                VisibilityDelegate(binding.tvNowPlaying)
            )
        )
        lyricsNowPlayingSlideDelegate.addDelegate(
            TransitionVisibilityDelegate(
                0.02f,
                0.04f,
                0.97f,
                0.99f,
                binding.transitionShadow,
            )
        )
        lyricsNowPlayingSlideDelegate.addDelegate(InvisibilityDelegate(binding.tvQueueTitle))
        lyricsNowPlayingSlideDelegate.addDelegate(InvisibilityDelegate(binding.tvQueueSubtitle))
    }

    fun initWithViewPager(viewPager: ViewPager2, initialPosition: Int) {
        // to prevent wrong state behavior
        // motion layout should have current transition as first as possible
        if (viewPager.adapter!!.itemCount == 2) {
            motionLayoutDelegate.setTransitionId(R.id.transition_lyrics_queue)
        } else if (initialPosition >= 1) {
            motionLayoutDelegate.setTransitionId(R.id.transition_now_playing_queue)
        } else {
            motionLayoutDelegate.setTransitionId(R.id.transition_lyrics_now_playing)
        }

        viewPager.onPageScrolled { position, positionOffset, _ ->
            if (viewPager.adapter!!.itemCount == 2) {
                // transition lyrics <-> play queue
                val offset = if (position == 1) 1 - positionOffset else positionOffset
                motionLayoutDelegate.setTransitionId(R.id.transition_lyrics_queue)
                lyricsPlayQueueSlideDelegate.onSlide(offset)
            } else if (position >= 1) {
                // transition nowPlaying <-> play queue
                val offset = if (position == 2) 1 - positionOffset else positionOffset
                motionLayoutDelegate.setTransitionId(R.id.transition_now_playing_queue)
                nowPlayingPlayQueueSlideDelegate.onSlide(offset)
            } else {
                // transition nowPlaying <-> lyrics
                motionLayoutDelegate.setTransitionId(R.id.transition_lyrics_now_playing)
                lyricsNowPlayingSlideDelegate.onSlide(positionOffset)
            }
        }
    }

    fun setTitleClickListener(onClick: OnClickListener) {
        binding.flTitleArea.setOnClickListener(onClick)
    }

}