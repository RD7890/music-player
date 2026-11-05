package com.github.anrimian.musicplayer.ui.aura

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.Pause as FillPause
import com.adamglin.phosphoricons.fill.Play as FillPlay
import com.adamglin.phosphoricons.fill.SkipBack as FillSkipBack
import com.adamglin.phosphoricons.fill.SkipForward as FillSkipForward
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.DotsSixVertical
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.ListPlus
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.PlayCircle
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.Faders
import com.adamglin.phosphoricons.regular.Gauge
import com.adamglin.phosphoricons.regular.ListBullets
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.MicrophoneStage
import com.adamglin.phosphoricons.regular.MusicNotes
import com.adamglin.phosphoricons.regular.Playlist
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.RepeatOnce
import com.adamglin.phosphoricons.regular.Shuffle
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.Timer
import com.adamglin.phosphoricons.regular.VinylRecord
import com.adamglin.phosphoricons.regular.X

/**
 * Single place that touches the Phosphor icon library.
 * If the library package layout ever changes, only this file needs to be fixed.
 */
object AuraIcons {
    val Play: ImageVector get() = PhosphorIcons.Fill.FillPlay
    val Pause: ImageVector get() = PhosphorIcons.Fill.FillPause
    val SkipForward: ImageVector get() = PhosphorIcons.Fill.FillSkipForward
    val SkipBack: ImageVector get() = PhosphorIcons.Fill.FillSkipBack

    val Shuffle: ImageVector get() = PhosphorIcons.Regular.Shuffle
    val Repeat: ImageVector get() = PhosphorIcons.Regular.Repeat
    val RepeatOnce: ImageVector get() = PhosphorIcons.Regular.RepeatOnce
    val Search: ImageVector get() = PhosphorIcons.Regular.MagnifyingGlass
    val Songs: ImageVector get() = PhosphorIcons.Regular.MusicNotes
    val Albums: ImageVector get() = PhosphorIcons.Regular.VinylRecord
    val Artists: ImageVector get() = PhosphorIcons.Regular.MicrophoneStage
    val Playlists: ImageVector get() = PhosphorIcons.Regular.Playlist
    val Collapse: ImageVector get() = PhosphorIcons.Regular.CaretDown
    val Close: ImageVector get() = PhosphorIcons.Regular.X
    val Queue: ImageVector get() = PhosphorIcons.Regular.ListBullets
    val SleepTimer: ImageVector get() = PhosphorIcons.Regular.Timer
    val Equalizer: ImageVector get() = PhosphorIcons.Regular.Faders
    val Speed: ImageVector get() = PhosphorIcons.Regular.Gauge
    val Orbit: ImageVector get() = PhosphorIcons.Regular.SquaresFour
    val Back: ImageVector get() = PhosphorIcons.Regular.ArrowLeft
    val Check: ImageVector get() = PhosphorIcons.Regular.Check
    val DragHandle: ImageVector get() = PhosphorIcons.Regular.DotsSixVertical
    val Info: ImageVector get() = PhosphorIcons.Regular.Info
    val AddToPlaylist: ImageVector get() = PhosphorIcons.Regular.ListPlus
    val Rename: ImageVector get() = PhosphorIcons.Regular.PencilSimple
    val PlayNext: ImageVector get() = PhosphorIcons.Regular.PlayCircle
    val Add: ImageVector get() = PhosphorIcons.Regular.Plus
    val Delete: ImageVector get() = PhosphorIcons.Regular.Trash
}
