package com.github.anrimian.fsync.models

class ProgressInfo(var current: Long = -1, var total: Long = -1) {

    fun set(current: Long, total: Long) {
        this.current = current
        this.total = total
    }

    override fun toString(): String {
        return "${asInt()}%"
    }

    fun asInt(): Int {
        if (total <= 0) {
            return -1
        }
        return ((current.toFloat() / total) * 100).toInt()
    }

}