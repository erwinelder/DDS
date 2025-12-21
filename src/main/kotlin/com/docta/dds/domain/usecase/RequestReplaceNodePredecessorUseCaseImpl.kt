package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class RequestReplaceNodePredecessorUseCaseImpl(
    private val client: HttpClient
) : RequestReplaceNodePredecessorUseCase {

    override suspend fun execute(
        targetNodeAddress: String,
        newPredecessorAddress: String,
        newPrePredecessorAddress: String
    ): SimpleResult<Error> {
        val service: NodeService = NodeRestControllerImpl(hostname = targetNodeAddress, client = client)

        return callCatching {
            service.replacePredecessor(
                newPredecessorAddress = newPredecessorAddress,
                newPrePredecessorAddress = newPrePredecessorAddress
            )
        }.getOrElse { return SimpleResult.Error(Error.ServiceNotAvailable) }
    }

}