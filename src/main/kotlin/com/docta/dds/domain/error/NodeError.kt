package com.docta.dds.domain.error

import kotlinx.serialization.Serializable

@Serializable
sealed class NodeError : Error {

    @Serializable object ServiceNotAvailable : NodeError()
    @Serializable object RegisterFailed : NodeError()
    @Serializable object InitiateLonelinessProtocolFailed : NodeError()
    @Serializable object ReplaceSuccessorsFailed : NodeError()
    @Serializable object ReplacePredecessorsFailed : NodeError()
    @Serializable object ProclaimLeaderFailed : NodeError()

    @Serializable object NodeIsAlreadyRegistered : NodeError()
    @Serializable object NodeIsNotRegisteredYet : NodeError()
    @Serializable object RingConsistsOnlyOfOneNode : NodeError()

}