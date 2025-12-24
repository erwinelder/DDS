package com.docta.dds.domain.model.core

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object AppContext {

    var messageDelay: Long = 0
        private set

    fun setMessageDelay(delayMs: Long) {
        messageDelay = delayMs
    }


    var successorStateCheckInterval: Long = 5000
        private set


    fun log(message: String) {
        val format = LocalDateTime.Format { hour(); char(':'); minute(); char(':'); second(); char(','); secondFraction() }
        val localDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val timestamp = localDateTime.format(format)

        println("$timestamp | $message")
    }

}