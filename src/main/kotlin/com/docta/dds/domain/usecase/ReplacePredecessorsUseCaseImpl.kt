package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ReplacePredecessorsUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : ReplacePredecessorsUseCase {

    override suspend fun execute(predecessors: List<String>): SimpleResult<Error> {
        nodeState.setPredecessors(addresses = predecessors)

        if (predecessors.isNotEmpty() && nodeState.successorAddress == nodeState.predecessorAddress) {
            nodeState.removeGrandSuccessor()
        }

        return SimpleResult.Success()
    }

    override suspend fun execute(
        targetNodeAddress: String,
        predecessors: List<String>
    ): ResultData<NodeStateDto, Error> {
        val service: NodeService = NodeRestControllerImpl(hostname = targetNodeAddress, client = client)

        return callCatching { service.replacePredecessors(predecessors = predecessors) }
            .getOrElse { return ResultData.Error(Error.ReplacePredecessorsFailed) }
    }

}