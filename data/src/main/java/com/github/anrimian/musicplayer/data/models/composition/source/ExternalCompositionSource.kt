package com.github.anrimian.musicplayer.data.models.composition.source

import android.net.Uri
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource

class ExternalCompositionSource(
    val uri: Uri,
    val displayName: String?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val size: Long,
) : CompositionSource {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExternalCompositionSource) return false

        if (uri != other.uri) return false

        return true
    }

    override fun hashCode(): Int {
        return uri.hashCode()
    }

    override fun toString(): String {
        return "UriCompositionSource{" +
                "uri=" + uri +
                ", displayName='" + displayName + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}'
    }

}
