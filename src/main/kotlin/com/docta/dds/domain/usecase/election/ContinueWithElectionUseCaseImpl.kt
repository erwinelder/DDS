package com.docta.dds.domain.usecase.election

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ContinueWithElectionUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext
) : ContinueWithElectionUseCase {

    override suspend fun execute(candidateId: String): SimpleResult<NodeError> {
        val successorAddress = nodeContext.successorAddress

        if (successorAddress == null) {
            nodeContext.proclaimAsLeader()
            return SimpleResult.Success()
        }

        val service: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)
        return callCatching { service.processElection(candidateId = candidateId) }
            .getOrElse { return SimpleResult.Error(NodeError.ServiceNotAvailable) }
    }

}