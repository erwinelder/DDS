package com.docta.dds.presentation.service

import com.docta.dds.domain.model.NodeState
import com.docta.dds.domain.usecase.RequestNodeRegistrationUseCase
import com.docta.dds.domain.usecase.RequestReplaceNodePredecessorUseCase
import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.network.context.asRoutingContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse
import io.ktor.server.plugins.*

class NodeServiceImpl(
    private val nodeState: NodeState,
    private val requestNodeRegistrationUseCase: RequestNodeRegistrationUseCase,
    private val requestReplaceNodePredecessorUseCase: RequestReplaceNodePredecessorUseCase
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
        if (nodeState.isRegistered()) return SimpleResult.Error(Error.NodeIsAlreadyRegistered)

        if (greeterIpAddress.isBlank()) {
            nodeState.registerNode()
            return SimpleResult.Success()
        }

        val registrationState = requestNodeRegistrationUseCase.execute(greeterIpAddress = greeterIpAddress)
            .getOrElse { return SimpleResult.Error(it) }
            .replaceNeighborIpAddressesIfNull(newAddress = greeterIpAddress)

        nodeState.registerNode(registrationState = registrationState)

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun registerNode(): ResultData<RegistrationStateDto, Error> {
        val ctx = ctx.asRoutingContext()

        val newNodeIp = ctx.call.request.origin.remoteAddress

        if (!nodeState.isRegistered()) return ResultData.Error(Error.NodeIsNotRegisteredYet)

        val neighborAddress = nodeState.successorAddress

        if (neighborAddress != null) {
            val result = requestReplaceNodePredecessorUseCase.execute(
                targetNodeIpAddress = neighborAddress, newIpAddress = newNodeIp
            )

            when (result) {
                is SimpleResult.Success -> {
                    val registrationState = RegistrationStateDto(successorIpAddress = neighborAddress)
                    nodeState.setSuccessor(address = newNodeIp)
                    return ResultData.Success(data = registrationState)
                }
                is SimpleResult.Error -> return ResultData.Error(result.error)
            }
        } else {
            nodeState.setSuccessor(address = newNodeIp)
            nodeState.setPredecessor(address = newNodeIp)

            return ResultData.Success(data = RegistrationStateDto())
        }
    }

    context(ctx: DrpcContext)
    override suspend fun replacePredecessor(newIpAddress: String): SimpleResult<Error> {
        nodeState.setPredecessor(address = newIpAddress)
        return SimpleResult.Success()
    }

}