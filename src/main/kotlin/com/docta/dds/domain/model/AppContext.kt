package com.docta.dds.domain.model

object AppContext {

    var predecessorStateCheckInterval: Long = 5000
        private set

    fun setPredecessorStateCheckInterval(intervalMs: Long) {
        predecessorStateCheckInterval = intervalMs
    }

}