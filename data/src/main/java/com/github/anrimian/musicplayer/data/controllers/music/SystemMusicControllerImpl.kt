package com.github.anrimian.musicplayer.data.controllers.music

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import com.github.anrimian.musicplayer.data.utils.rx.audio_focus.RxAudioFocus
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.volume.VolumeObserver
import com.github.anrimian.musicplayer.data.utils.rx.receivers.RxReceivers
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import io.reactivex.rxjava3.core.Observable

/**
 * Created on 10.12.2017.
 */
class SystemMusicControllerImpl(private val context: Context) : SystemMusicController {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var volumeState: VolumeState? = null

    override fun requestAudioFocus(): Observable<AudioFocusEvent>? {
        return RxAudioFocus.requestAudioFocus(
            audioManager,
            AudioAttributes.USAGE_MEDIA,
            AudioAttributes.CONTENT_TYPE_MUSIC,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun getAudioBecomingNoisyObservable(): Observable<Any> {
        return RxReceivers.from(AudioManager.ACTION_AUDIO_BECOMING_NOISY, context)
            .map { Constants.TRIGGER }
    }

    override fun getVolumeObservable(): Observable<Int> {
        return VolumeObserver.getVolumeObservable(context, audioManager)
    }

    override fun getVolumeStateObservable(): Observable<VolumeState> {
        return VolumeObserver.getVolumeStateObservable(context, audioManager)
            .doOnNext { volumeState -> this.volumeState = volumeState }
    }

    override fun getVolumeState(): VolumeState {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = VolumeObserver.safeGetStreamVolume(audioManager)
        return VolumeState.from(volume, maxVolume)
    }

    override fun setVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (volume <= maxVolume && volume >= 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        }
    }

    override fun changeVolumeBy(volume: Int) {
        if (volumeState != null) {
            setVolume(volumeState!!.getVolume() + volume)
        }
    }
}
