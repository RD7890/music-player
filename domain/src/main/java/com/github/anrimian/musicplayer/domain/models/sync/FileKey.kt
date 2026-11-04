package com.github.anrimian.musicplayer.domain.models.sync

import com.github.anrimian.musicplayer.domain.utils.normalize

class FileKey(val path: String) {

    constructor(name: String, parentPath: String) : this(
        when {
            parentPath.isEmpty() -> name
            parentPath.endsWith("/") -> "$parentPath$name"
            else -> "$parentPath/$name"
        }
    )

    val name by lazy { path.substringAfterLast('/', path) }
    val parentPath by lazy { path.substringBeforeLast('/', "") }

    private val normalizedPath = normalize(path)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileKey) return false

        if (normalizedPath != other.normalizedPath) return false

        return true
    }

    override fun hashCode(): Int {
        return normalizedPath.hashCode()
    }

    override fun toString(): String {
        return path
    }

}