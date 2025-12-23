package com.docta.dds.domain.model.core

object AppContext {

    var successorStateCheckInterval: Long = 5000
        private set

    fun setSuccessorStateCheckInterval(intervalMs: Long) {
        successorStateCheckInterval = intervalMs
    }

}