package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ReplaceSuccessorsUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : ReplaceSuccessorsUseCase {

    override suspend fun execute(successors: List<String>): SimpleResult<Error> {
        nodeState.setSuccessors(addresses = successors)

        if (successors.isNotEmpty() && nodeState.successorAddress == nodeState.predecessorAddress) {
            nodeState.removeGrandSuccessor()
        }

        val predecessorAddress = nodeState.predecessorAddress ?: return SimpleResult.Error(Error.RingConsistsOnlyOfOneNode)
        val service: NodeService = NodeRestControllerImpl(hostname = predecessorAddress, client = client)

        val nextSuccessors = listOfNotNull(
            nodeState.nodeAddress,
            nodeState.successorAddress
        )
        return callCatching { service.replaceSuccessors(successors = nextSuccessors) }
            .getOrElse { return SimpleResult.Error(Error.ReplaceSuccessorsFailed) }
    }

}