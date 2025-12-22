package com.docta.dds.error

import kotlinx.serialization.Serializable

@Serializable
sealed class Error {

    @Serializable object ServiceNotAvailable : Error()
    @Serializable object RegisterFailed : Error()
    @Serializable object InitiateLonelinessProtocolFailed : Error()
    @Serializable object ReplaceSuccessorsFailed : Error()
    @Serializable object ReplacePredecessorsFailed : Error()
    @Serializable object ProclaimLeaderFailed : Error()

    @Serializable object NodeIsAlreadyRegistered : Error()
    @Serializable object NodeIsNotRegisteredYet : Error()
    @Serializable object RingConsistsOnlyOfOneNode : Error()

}