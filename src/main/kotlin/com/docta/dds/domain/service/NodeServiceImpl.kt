package com.docta.dds.domain.service

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.model.core.AppContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.model.node.NodeState
import com.docta.dds.domain.model.node.RegistrationState
import com.docta.dds.domain.usecase.node.*
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
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): getting state.")

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
    override suspend fun setMessageDelay(delayMs: Long): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): setting message delay to $delayMs ms.")

        AppContext.setMessageDelay(delayMs = delayMs)
        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun isAlive(): SimpleResult<NodeError> {
        return SimpleResult.Success()
    }


    @OptIn(ExperimentalUuidApi::class)
    context(ctx: DrpcContext)
    override suspend fun join(greeterIpAddress: String): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): joining the ring via greeter at '$greeterIpAddress'.")

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

        AppContext.log(message = "Node (${nodeContext.nodeAddress}): registering new node at address '$newNodeAddress'.")

        return registerNodeUseCase.execute(newNodeAddress = newNodeAddress)
    }

    context(ctx: DrpcContext)
    override suspend fun leave(): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): leaving the ring.")

        leaveRingUseCase.execute().onError { return SimpleResult.Error(it) }

        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            exitProcess(0)
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun kill(): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): killed.")

        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            exitProcess(0)
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun replaceSuccessors(successors: List<String>): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): replacing successors with '${successors.joinToString()}'.")

        if (!nodeContext.isRegistered()) return SimpleResult.Success()
        if (nodeContext.successorsEqual(other = successors)) return SimpleResult.Success()

        return replaceSuccessorsUseCase.execute(successors = successors)
    }

    context(ctx: DrpcContext)
    override suspend fun replacePredecessors(predecessors: List<String>): ResultData<NodeState, NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): replacing predecessors with '${predecessors.joinToString()}'.")

        if (nodeContext.predecessorsEqual(other = predecessors)) return getState()

        replacePredecessorsUseCase.execute(predecessors = predecessors).onError { return ResultData.Error(it) }

        return getState()
    }


    context(ctx: DrpcContext)
    override suspend fun startElection(): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): starting election.")

        return proclaimLeaderUseCase.execute(
            leaderId = nodeContext.getNodeIdString(), leaderAddress = nodeContext.nodeAddress, chatState = ChatState()
        )
    }

    context(ctx: DrpcContext)
    override suspend fun proclaimLeader(leaderId: String, leaderAddress: String, chatState: ChatState): SimpleResult<NodeError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): proclaiming leader with ID $leaderId at address '$leaderAddress', the latest chat message sequence is ${chatState.seq}.")

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
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): initiating loneliness protocol.")

        initiateLonelinessProtocolUseCase.execute()
        return SimpleResult.Success()
    }

}