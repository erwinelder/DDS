package com.docta.dds.presentation.controller

import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.node.NodeState
import com.docta.dds.domain.model.node.RegistrationState
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
    override suspend fun getState(): ResultData<NodeState, NodeError> = client.callPost(
        url = absoluteUrl + getStatePath
    )

    context(ctx: DrpcContext)
    override suspend fun setMessageDelay(
        delayMs: Long
    ): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + setMessageDelayPath,
        delayMs.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun isAlive(): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + isAlivePath
    )


    context(ctx: DrpcContext)
    override suspend fun join(
        greeterIpAddress: String
    ): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + joinPath,
        greeterIpAddress.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationState, NodeError> = client.callPost(
        url = absoluteUrl + registerNodePath
    )

    context(ctx: DrpcContext)
    override suspend fun leave(): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + leavePath
    )

    context(ctx: DrpcContext)
    override suspend fun kill(): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + killPath
    )

    context(ctx: DrpcContext)
    override suspend fun replaceSuccessors(
        successors: List<String>
    ): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + replaceSuccessorsPath,
        successors.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun replacePredecessors(
        predecessors: List<String>
    ): ResultData<NodeState, NodeError> = client.callPost(
        url = absoluteUrl + replacePredecessorsPath,
        predecessors.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun initiateLonelinessProtocol(): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + initiateLonelinessProtocolPath
    )


    context(ctx: DrpcContext)
    override suspend fun startElection(): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + startElectionPath
    )

    context(ctx: DrpcContext)
    override suspend fun processElection(
        candidateId: String
    ): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + processElectionPath,
        candidateId.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun finishElection(
        newLeaderId: String,
        newLeaderAddress: String,
        chatState: ChatState
    ): SimpleResult<NodeError> = client.callPost(
        url = absoluteUrl + finishElectionPath,
        newLeaderId.asCallParameter(),
        newLeaderAddress.asCallParameter(),
        chatState.asCallParameter()
    )

}