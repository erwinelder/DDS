package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class CheckPredecessorIsAliveUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : CheckPredecessorIsAliveUseCase {

    override suspend fun execute(): SimpleResult<Error> {
        val predecessorAddress = nodeState.predecessorAddress ?: return SimpleResult.Success()
        val service: NodeService = NodeRestControllerImpl(hostname = predecessorAddress, client = client)

        return callCatching { service.isAlive() }.getOrElse { SimpleResult.Error(Error.ServiceNotAvailable) }
    }

}