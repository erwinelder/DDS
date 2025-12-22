package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class CheckSuccessorIsAliveUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : CheckSuccessorIsAliveUseCase {

    override suspend fun execute(): SimpleResult<Error> {
        val successorAddress = nodeState.successorAddress ?: return SimpleResult.Success()
        val service: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)

        return callCatching { service.isAlive() }.getOrElse { return SimpleResult.Error(Error.ServiceNotAvailable) }
    }

}