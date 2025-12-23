package com.docta.dds.domain.model.core

object AppContext {

    var messageDelay: Long = 0
        private set

    fun setMessageDelay(delayMs: Long) {
        messageDelay = delayMs
    }


    var successorStateCheckInterval: Long = 5000
        private set

}