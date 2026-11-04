package com.github.anrimian.musicplayer.ui.main.external_player

import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.folders.FileReference
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.rxjava3.core.Scheduler

class ExternalPlayerPresenter(
    private val interactor: ExternalPlayerInteractor,
    uiScheduler: Scheduler,
    errorParser: ErrorParser
) : AppPresenter<ExternalPlayerView>(uiScheduler, errorParser) {
    
    private var currentPosition: Long = 0
    private var currentSource: ExternalCompositionSource? = null

    private var playerError: ErrorCommand? = null
    private var prepareError: ErrorCommand? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showKeepPlayerInBackground(interactor.isExternalPlayerKeepInBackground())

        interactor.getCurrentSourceObservable()
            .unsafeSubscribeOnUi(this::onCompositionSourceReceived)

        interactor.getIsPlayingStateObservable().unsafeSubscribeOnUi(viewState::showPlayerState)
        interactor.getTrackPositionObservable().unsafeSubscribeOnUi(this::onTrackPositionChanged)
        interactor.getExternalPlayerRepeatModeObservable().unsafeSubscribeOnUi(viewState::showRepeatMode)
        interactor.getPlayerStateObservable().unsafeSubscribeOnUi(this::onPlayerStateReceived)
        interactor.getSpeedChangeAvailableObservable().unsafeSubscribeOnUi(viewState::showSpeedVisible)
        interactor.getPlaybackSpeedObservable().unsafeSubscribeOnUi(viewState::displayPlaybackSpeed)
        interactor.getVolumeObservable()
            .map(VolumeState::toLong)
            .unsafeSubscribeOnUi(viewState::showVolumeState)
        interactor.getResetSignalObservable().unsafeSubscribeOnUi { viewState.closeScreen() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!interactor.isExternalPlayerKeepInBackground()) {
            interactor.reset()
        }
    }

    fun onFileReferenceReceived(fileRef: FileReference) {
        interactor.startPlaying(fileRef)
            .justRunOnUi { error ->
                prepareError = error
                updateErrorState()
            }
    }

    fun onCompositionSourceReceived(sourceOpt: Opt<CompositionSource>) {
        val source = sourceOpt.value
        if (source !is ExternalCompositionSource?) {
            throw IllegalArgumentException()
        }
        this.currentSource = source
        viewState.displayComposition(source)
        if (source == null) {
            viewState.showTrackState(0, 0)
        } else {
            viewState.showTrackState(currentPosition, source.duration)

            prepareError = null
            updateErrorState()
        }
    }

    fun onPlayPauseClicked() {
        interactor.playOrPause()
    }

    fun onTrackRewoundTo(progress: Int) {
        interactor.seekTo(progress.toLong())
    }

    fun onSeekStart() {
        interactor.onSeekStarted()
    }

    fun onSeekStop(progress: Int) {
        interactor.onSeekFinished(progress.toLong())
    }

    fun onRepeatModeButtonClicked() {
        interactor.changeExternalPlayerRepeatMode()
    }

    fun onKeepPlayerInBackgroundChecked(checked: Boolean) {
        interactor.setExternalPlayerKeepInBackground(checked)
    }

    fun onFastSeekForwardCalled() {
        interactor.fastSeekForward()
    }

    fun onFastSeekBackwardCalled() {
        interactor.fastSeekBackward()
    }

    fun onPlaybackSpeedSelected(speed: Float) {
        viewState.displayPlaybackSpeed(speed)
        interactor.setPlaybackSpeed(speed)
    }

    private fun onPlayerStateReceived(playerState: PlayerState) {
        playerError = if (playerState is PlayerState.Error) {
            errorParser.parseError(playerState.throwable)
        } else {
            null
        }
        updateErrorState()
    }

    private fun updateErrorState() {
        viewState.showPlayErrorState( playerError ?: prepareError)
    }

    private fun onTrackPositionChanged(currentPosition: Long) {
        this.currentPosition = currentPosition
        val source = currentSource
        if (source != null) {
            val duration = source.duration
            viewState.showTrackState(currentPosition, duration)
        }
    }

}