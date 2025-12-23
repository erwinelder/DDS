package com.docta.dds.domain.error

import kotlinx.serialization.Serializable

@Serializable
sealed class ChatError : Error {

    @Serializable object ServiceNotAvailable : ChatError()

}