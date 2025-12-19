package com.docta.dds.presentation.service

import com.docta.dds.domain.model.NodeState
import com.docta.dds.domain.usecase.JoinRingUseCase
import com.docta.dds.domain.usecase.RegisterNodeUseCase
import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.network.context.asRoutingContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import io.ktor.server.plugins.*

class NodeServiceImpl(
    private val nodeState: NodeState,
    private val joinRingUseCase: JoinRingUseCase,
    private val registerNodeUseCase: RegisterNodeUseCase
) : NodeService {

    context(ctx: DrpcContext)
    override suspend fun getState(): ResultData<NodeStateDto, Error> {
        val nodeState = NodeStateDto(
            nodeId = nodeState.getNodeIdOrNull(),
            isLeader = nodeState.isLeader,
            successorAddress = nodeState.successorAddress,
            predecessorAddress = nodeState.predecessorAddress
        )
        return ResultData.Success(data = nodeState)
    }


    context(ctx: DrpcContext)
    override suspend fun join(greeterIpAddress: String): SimpleResult<Error> {
        return joinRingUseCase.execute(greeterIpAddress = greeterIpAddress)
    }

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationStateDto, Error> {
        val ctx = ctx.asRoutingContext()
        val newNodeAddress = ctx.call.request.origin.remoteAddress

        return registerNodeUseCase.execute(newNodeAddress = newNodeAddress)
    }

    context(ctx: DrpcContext)
    override suspend fun replacePredecessor(newIpAddress: String): SimpleResult<Error> {
        nodeState.setPredecessor(address = newIpAddress)
        return SimpleResult.Success()
    }

}