package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.error.NodeError
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class CheckSuccessorIsAliveUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext
) : CheckSuccessorIsAliveUseCase {

    override suspend fun execute(): SimpleResult<NodeError> {
        val successorAddress = nodeContext.successorAddress ?: return SimpleResult.Success()
        val service: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)

        return callCatching { service.isAlive() }.getOrElse { return SimpleResult.Error(NodeError.ServiceNotAvailable) }
    }

}