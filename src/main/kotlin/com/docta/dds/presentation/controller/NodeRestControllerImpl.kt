package com.docta.dds.presentation.controller

import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.network.asCallParameter
import com.docta.drpc.core.network.client.callPost
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class NodeRestControllerImpl(
    override val hostname: String,
    private val client: HttpClient
) : NodeRestController {

    context(ctx: DrpcContext)
    override suspend fun getState(): ResultData<NodeStateDto, Error> = client.callPost(
        url = absoluteUrl + getStatePath
    )

    context(ctx: DrpcContext)
    override suspend fun isAlive(): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + isAlivePath
    )


    context(ctx: DrpcContext)
    override suspend fun join(greeterIpAddress: String): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + joinPath,
        greeterIpAddress.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationStateDto, Error> = client.callPost(
        url = absoluteUrl + registerNodePath
    )

    context(ctx: DrpcContext)
    override suspend fun leave(): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + leavePath
    )

    context(ctx: DrpcContext)
    override suspend fun replaceSuccessor(
        newIpAddress: String
    ): ResultData<String?, Error> = client.callPost(
        url = absoluteUrl + replaceSuccessorPath,
        newIpAddress.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun replacePredecessor(
        newPredecessorAddress: String,
        newPrePredecessorAddress: String
    ): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + replacePredecessorPath,
        newPredecessorAddress.asCallParameter(),
        newPrePredecessorAddress.asCallParameter()
    )


    context(ctx: DrpcContext)
    override suspend fun proclaimLeader(
        leaderId: String,
        leaderAddress: String
    ): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + proclaimLeaderPath,
        leaderId.asCallParameter(),
        leaderAddress.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun initiateLonelinessProtocol(): SimpleResult<Error> = client.callPost(
        url = absoluteUrl + initiateLonelinessProtocolPath
    )

}