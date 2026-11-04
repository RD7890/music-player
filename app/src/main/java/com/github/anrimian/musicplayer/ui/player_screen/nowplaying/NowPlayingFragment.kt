package com.github.anrimian.musicplayer.ui.player_screen.nowplaying

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment: Fragment() {

    private lateinit var binding: FragmentNowPlayingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.ivBigCover.onLongClick(::disableNowPlayingScreen)
    }

    private fun disableNowPlayingScreen() {
        val currentFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.main_activity_container)
//        if (currentFragment is PlayerFragment) {
//            currentFragment.disableNowPlayingScreen()
//        }
    }
}