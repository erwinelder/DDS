package com.docta.dds.domain.service

import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.error.NodeError
import com.docta.dds.domain.model.node.NodeState
import com.docta.dds.domain.model.node.RegistrationState
import com.docta.dds.domain.usecase.node.InitiateLonelinessProtocolUseCase
import com.docta.dds.domain.usecase.node.JoinRingUseCase
import com.docta.dds.domain.usecase.node.LeaveRingUseCase
import com.docta.dds.domain.usecase.node.ProclaimLeaderUseCase
import com.docta.dds.domain.usecase.node.RegisterNodeUseCase
import com.docta.dds.domain.usecase.node.ReplacePredecessorsUseCase
import com.docta.dds.domain.usecase.node.ReplaceSuccessorsUseCase
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.network.context.asRoutingContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.onError
import io.ktor.server.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi

class NodeServiceImpl(
    private val nodeContext: NodeContext,
    private val joinRingUseCase: JoinRingUseCase,
    private val registerNodeUseCase: RegisterNodeUseCase,
    private val leaveRingUseCase: LeaveRingUseCase,
    private val proclaimLeaderUseCase: ProclaimLeaderUseCase,
    private val initiateLonelinessProtocolUseCase: InitiateLonelinessProtocolUseCase,
    private val replaceSuccessorsUseCase: ReplaceSuccessorsUseCase,
    private val replacePredecessorsUseCase: ReplacePredecessorsUseCase
) : NodeService {

    context(ctx: DrpcContext)
    override suspend fun getState(): ResultData<NodeState, NodeError> {
        val nodeState = NodeState(
            nodeId = nodeContext.getNodeIdStringOrNull(),
            nodeAddress = nodeContext.nodeAddress,
            leaderId = nodeContext.getLeaderIdStringOrNull(),
            leaderAddress = nodeContext.leaderAddress,
            isLeader = nodeContext.isLeader,
            successorAddress = nodeContext.successorAddress,
            grandSuccessorAddress = nodeContext.grandSuccessorAddress,
            predecessorAddress = nodeContext.predecessorAddress
        )
        return ResultData.Success(data = nodeState)
    }

    context(ctx: DrpcContext)
    override suspend fun isAlive(): SimpleResult<NodeError> {
        return SimpleResult.Success()
    }


    @OptIn(ExperimentalUuidApi::class)
    context(ctx: DrpcContext)
    override suspend fun join(greeterIpAddress: String): SimpleResult<NodeError> {
        joinRingUseCase.execute(greeterIpAddress = greeterIpAddress.ifBlank { null })
            .onError { return SimpleResult.Error(it) }

        if (nodeContext.getNodeId() > nodeContext.getLeaderId()) {
            proclaimLeaderUseCase.execute(
                leaderId = nodeContext.getNodeIdString(),
                leaderAddress = nodeContext.nodeAddress,
                chatState = ChatState()
            ).onError { return SimpleResult.Error(it) }
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationState, NodeError> {
        val ctx = ctx.asRoutingContext()
        val newNodeAddress = ctx.call.request.origin.remoteAddress

        return registerNodeUseCase.execute(newNodeAddress = newNodeAddress)
    }

    context(ctx: DrpcContext)
    override suspend fun leave(): SimpleResult<NodeError> {
        leaveRingUseCase.execute().onError { return SimpleResult.Error(it) }

        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            exitProcess(0)
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun kill(): SimpleResult<NodeError> {
        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            exitProcess(0)
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun replaceSuccessors(successors: List<String>): SimpleResult<NodeError> {
        if (!nodeContext.isRegistered()) return SimpleResult.Success()
        if (nodeContext.successorsEqual(other = successors)) return SimpleResult.Success()

        return replaceSuccessorsUseCase.execute(successors = successors)
    }

    context(ctx: DrpcContext)
    override suspend fun replacePredecessors(predecessors: List<String>): ResultData<NodeState, NodeError> {
        if (nodeContext.predecessorsEqual(other = predecessors)) return getState()

        replacePredecessorsUseCase.execute(predecessors = predecessors).onError { return ResultData.Error(it) }

        return getState()
    }


    context(ctx: DrpcContext)
    override suspend fun proclaimLeader(leaderId: String, leaderAddress: String, chatState: ChatState): SimpleResult<NodeError> {
        return when {
            nodeContext.getNodeIdString() > leaderId -> proclaimLeaderUseCase.execute(
                leaderId = nodeContext.getNodeIdString(), leaderAddress = nodeContext.nodeAddress, chatState = chatState
            )
            nodeContext.getNodeIdString() < leaderId -> proclaimLeaderUseCase.execute(
                leaderId = leaderId, leaderAddress = leaderAddress, chatState = chatState
            )
            else -> {
                nodeContext.proclaimAsLeader()
                SimpleResult.Success()
            }
        }
    }

    context(ctx: DrpcContext)
    override suspend fun initiateLonelinessProtocol(): SimpleResult<NodeError> {
        initiateLonelinessProtocolUseCase.execute()
        return SimpleResult.Success()
    }

}