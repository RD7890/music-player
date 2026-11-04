package com.github.anrimian.musicplayer.domain.controllers

import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import io.reactivex.rxjava3.core.Observable

/**
 * Created on 10.12.2017.
 */
interface SystemMusicController {

    fun requestAudioFocus(): Observable<AudioFocusEvent>?

    fun getAudioBecomingNoisyObservable(): Observable<Any>

    fun getVolumeObservable(): Observable<Int>

    fun getVolumeStateObservable(): Observable<VolumeState>

    fun getVolumeState(): VolumeState

    fun setVolume(volume: Int)

    fun changeVolumeBy(volume: Int)

}
