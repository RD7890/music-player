package com.github.anrimian.musicplayer.ui.aura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow

/** Host of the Aura UI: a single Compose screen on top of the existing playback engine. */
class AuraFragment : Fragment() {

    companion object {
        private const val OPEN_PLAYER_ARG = "open_player"

        fun newInstance(openPlayer: Boolean) = AuraFragment().apply {
            arguments = Bundle().apply { putBoolean(OPEN_PLAYER_ARG, openPlayer) }
        }
    }

    private val openPlayerSignal = mutableIntStateOf(0)
    private var previousLightStatusBars: Boolean? = null

    fun openPlayer() {
        openPlayerSignal.intValue = openPlayerSignal.intValue + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && arguments?.getBoolean(OPEN_PLAYER_ARG) == true) {
            openPlayer()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val controller = remember { AuraController(Components.getAppComponent()) }
                AuraApp(
                    controller = controller,
                    openPlayerSignal = openPlayerSignal.intValue,
                    onSleepTimer = { SleepTimerDialogFragment().safeShow(childFragmentManager) },
                    onEqualizer = { EqualizerDialogFragment().safeShow(childFragmentManager) }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = requireActivity().window
        val controller = WindowCompat.getInsetsController(window, view)
        previousLightStatusBars = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
    }

    override fun onDestroyView() {
        val previous = previousLightStatusBars
        if (previous != null) {
            val window = requireActivity().window
            WindowCompat.getInsetsController(window, requireView()).isAppearanceLightStatusBars = previous
        }
        super.onDestroyView()
    }
}
