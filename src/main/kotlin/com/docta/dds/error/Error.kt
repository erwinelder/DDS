package com.docta.dds.error

import kotlinx.serialization.Serializable

@Serializable
sealed class Error {

    @Serializable object ServiceNotAvailable : Error()

    @Serializable object NodeIsAlreadyRegistered : Error()
    @Serializable object NodeIsNotRegisteredYet : Error()

}