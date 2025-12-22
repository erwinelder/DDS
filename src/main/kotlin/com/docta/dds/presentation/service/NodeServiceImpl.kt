package com.docta.dds.presentation.service

import com.docta.dds.domain.model.NodeState
import com.docta.dds.domain.usecase.*
import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
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
    private val nodeState: NodeState,
    private val joinRingUseCase: JoinRingUseCase,
    private val registerNodeUseCase: RegisterNodeUseCase,
    private val leaveRingUseCase: LeaveRingUseCase,
    private val proclaimLeaderUseCase: ProclaimLeaderUseCase,
    private val initiateLonelinessProtocolUseCase: InitiateLonelinessProtocolUseCase,
    private val replaceSuccessorsUseCase: ReplaceSuccessorsUseCase,
    private val replacePredecessorsUseCase: ReplacePredecessorsUseCase
) : NodeService {

    context(ctx: DrpcContext)
    override suspend fun getState(): ResultData<NodeStateDto, Error> {
        val nodeState = NodeStateDto(
            nodeId = nodeState.getNodeIdStringOrNull(),
            nodeAddress = nodeState.nodeAddress,
            leaderId = nodeState.getLeaderIdStringOrNull(),
            leaderAddress = nodeState.leaderAddress,
            isLeader = nodeState.isLeader,
            successorAddress = nodeState.successorAddress,
            grandSuccessorAddress = nodeState.grandSuccessorAddress,
            predecessorAddress = nodeState.predecessorAddress
        )
        return ResultData.Success(data = nodeState)
    }

    context(ctx: DrpcContext)
    override suspend fun isAlive(): SimpleResult<Error> {
        return SimpleResult.Success()
    }


    @OptIn(ExperimentalUuidApi::class)
    context(ctx: DrpcContext)
    override suspend fun join(greeterIpAddress: String): SimpleResult<Error> {
        joinRingUseCase.execute(greeterIpAddress = greeterIpAddress.ifBlank { null })
            .onError { return SimpleResult.Error(it) }

        if (nodeState.getNodeId() > nodeState.getLeaderId()) {
            proclaimLeaderUseCase
                .execute(leaderId = nodeState.getNodeIdString(), leaderAddress = nodeState.nodeAddress)
                .onError { return SimpleResult.Error(it) }
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationStateDto, Error> {
        val ctx = ctx.asRoutingContext()
        val newNodeAddress = ctx.call.request.origin.remoteAddress

        return registerNodeUseCase.execute(newNodeAddress = newNodeAddress)
    }

    context(ctx: DrpcContext)
    override suspend fun leave(): SimpleResult<Error> {
        leaveRingUseCase.execute().onError { return SimpleResult.Error(it) }

        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            exitProcess(0)
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun replaceSuccessors(successors: List<String>): SimpleResult<Error> {
        if (!nodeState.isRegistered()) return SimpleResult.Success()
        if (nodeState.successorsEqual(other = successors)) return SimpleResult.Success()

        return replaceSuccessorsUseCase.execute(successors = successors)
    }

    context(ctx: DrpcContext)
    override suspend fun replacePredecessors(predecessors: List<String>): ResultData<NodeStateDto, Error> {
        if (nodeState.predecessorsEqual(other = predecessors)) return getState()

        replacePredecessorsUseCase.execute(predecessors = predecessors).onError { return ResultData.Error(it) }

        return getState()
    }


    context(ctx: DrpcContext)
    override suspend fun proclaimLeader(leaderId: String, leaderAddress: String): SimpleResult<Error> {
        return when {
            nodeState.getNodeIdString() > leaderId -> proclaimLeaderUseCase.execute(
                leaderId = nodeState.getNodeIdString(), leaderAddress = nodeState.nodeAddress
            )
            nodeState.getNodeIdString() < leaderId -> proclaimLeaderUseCase.execute(
                leaderId = leaderId, leaderAddress = leaderAddress
            )
            else -> {
                nodeState.proclaimAsLeader()
                SimpleResult.Success()
            }
        }
    }

    context(ctx: DrpcContext)
    override suspend fun initiateLonelinessProtocol(): SimpleResult<Error> {
        initiateLonelinessProtocolUseCase.execute()
        return SimpleResult.Success()
    }

}