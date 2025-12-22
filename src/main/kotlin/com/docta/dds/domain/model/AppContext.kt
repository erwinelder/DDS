package com.docta.dds.domain.model

object AppContext {

    var successorStateCheckInterval: Long = 5000
        private set

    fun setSuccessorStateCheckInterval(intervalMs: Long) {
        successorStateCheckInterval = intervalMs
    }

}