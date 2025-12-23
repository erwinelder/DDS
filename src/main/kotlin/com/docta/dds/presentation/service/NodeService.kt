package com.docta.dds.presentation.service

import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.error.NodeError
import com.docta.dds.domain.model.node.NodeState
import com.docta.dds.domain.model.node.RegistrationState
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult

interface NodeService {

    context(ctx: DrpcContext)
    suspend fun getState(): ResultData<NodeState, NodeError>

    context(ctx: DrpcContext)
    suspend fun isAlive(): SimpleResult<NodeError>


    context(ctx: DrpcContext)
    suspend fun join(greeterIpAddress: String): SimpleResult<NodeError>

    context(ctx: DrpcContext)
    suspend fun registerNode(): ResultData<RegistrationState, NodeError>

    context(ctx: DrpcContext)
    suspend fun leave(): SimpleResult<NodeError>

    context(ctx: DrpcContext)
    suspend fun kill(): SimpleResult<NodeError>

    context(ctx: DrpcContext)
    suspend fun replaceSuccessors(successors: List<String>): SimpleResult<NodeError>

    context(ctx: DrpcContext)
    suspend fun replacePredecessors(predecessors: List<String>): ResultData<NodeState, NodeError>


    context(ctx: DrpcContext)
    suspend fun proclaimLeader(leaderId: String, leaderAddress: String, chatState: ChatState): SimpleResult<NodeError>

    context(ctx: DrpcContext)
    suspend fun initiateLonelinessProtocol(): SimpleResult<NodeError>

}