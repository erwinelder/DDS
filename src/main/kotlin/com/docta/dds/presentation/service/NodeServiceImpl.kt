package com.docta.dds.presentation.service

import com.docta.dds.domain.model.NodeState
import com.docta.dds.domain.usecase.JoinRingUseCase
import com.docta.dds.domain.usecase.ProclaimLeaderUseCase
import com.docta.dds.domain.usecase.RegisterNodeUseCase
import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.network.context.asRoutingContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.onError
import io.ktor.server.plugins.*
import kotlin.uuid.ExperimentalUuidApi

class NodeServiceImpl(
    private val nodeState: NodeState,
    private val joinRingUseCase: JoinRingUseCase,
    private val registerNodeUseCase: RegisterNodeUseCase,
    private val proclaimLeaderUseCase: ProclaimLeaderUseCase
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
            predecessorAddress = nodeState.predecessorAddress,
            predecessorOfPredecessorAddress = nodeState.prePredecessorAddress
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
        joinRingUseCase.execute(greeterIpAddress = greeterIpAddress).onError { return SimpleResult.Error(it) }

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
    override suspend fun replaceSuccessor(newIpAddress: String): ResultData<String?, Error> {
        val newIpAddress = newIpAddress.ifBlank {
            ctx.asRoutingContext().call.request.origin.remoteAddress
        }

        if (nodeState.prePredecessorAddress == nodeState.successorAddress) {
            nodeState.setPrePredecessor(address = null)
        }
        nodeState.setSuccessor(address = newIpAddress)

        return ResultData.Success(data = nodeState.predecessorAddress)
    }

    context(ctx: DrpcContext)
    override suspend fun replacePredecessor(newIpAddress: String): SimpleResult<Error> {
        val ctx = ctx.asRoutingContext()
        val currPredecessorAddress = ctx.call.request.origin.remoteAddress

        nodeState.setPredecessor(address = newIpAddress)
        nodeState.setPrePredecessor(address = currPredecessorAddress)

        return SimpleResult.Success()
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

}