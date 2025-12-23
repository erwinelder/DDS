package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.error.NodeError
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.domain.model.node.NodeState
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ReplacePredecessorsUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext
) : ReplacePredecessorsUseCase {

    override suspend fun execute(predecessors: List<String>): SimpleResult<NodeError> {
        nodeContext.setPredecessors(addresses = predecessors)

        if (predecessors.isNotEmpty() && nodeContext.successorAddress == nodeContext.predecessorAddress) {
            nodeContext.removeGrandSuccessor()
        }

        return SimpleResult.Success()
    }

    override suspend fun execute(
        targetNodeAddress: String,
        predecessors: List<String>
    ): ResultData<NodeState, NodeError> {
        val service: NodeService = NodeRestControllerImpl(hostname = targetNodeAddress, client = client)

        return callCatching { service.replacePredecessors(predecessors = predecessors) }
            .getOrElse { return ResultData.Error(NodeError.ReplacePredecessorsFailed) }
    }

}