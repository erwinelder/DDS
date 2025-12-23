package com.docta.dds.error

import kotlinx.serialization.Serializable

@Serializable
sealed class ChatError : Error {

    @Serializable object ServiceNotAvailable : ChatError()

}