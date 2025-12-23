package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.error.NodeError
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ReplaceSuccessorsUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext
) : ReplaceSuccessorsUseCase {

    override suspend fun execute(successors: List<String>): SimpleResult<NodeError> {
        nodeContext.setSuccessors(addresses = successors)

        if (successors.isNotEmpty() && nodeContext.successorAddress == nodeContext.predecessorAddress) {
            nodeContext.removeGrandSuccessor()
        }

        val predecessorAddress = nodeContext.predecessorAddress ?: return SimpleResult.Error(NodeError.RingConsistsOnlyOfOneNode)
        val service: NodeService = NodeRestControllerImpl(hostname = predecessorAddress, client = client)

        val nextSuccessors = listOfNotNull(
            nodeContext.nodeAddress,
            nodeContext.successorAddress
        )
        return callCatching { service.replaceSuccessors(successors = nextSuccessors) }
            .getOrElse { return SimpleResult.Error(NodeError.ReplaceSuccessorsFailed) }
    }

}