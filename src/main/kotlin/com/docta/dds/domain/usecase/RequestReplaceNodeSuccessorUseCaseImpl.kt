package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.ResultData
import io.ktor.client.*

class RequestReplaceNodeSuccessorUseCaseImpl(
    private val client: HttpClient
) : RequestReplaceNodeSuccessorUseCase {

    override suspend fun execute(
        targetNodeIpAddress: String,
        newIpAddress: String?
    ): ResultData<String?, Error> {
        val service: NodeService = NodeRestControllerImpl(hostname = targetNodeIpAddress, client = client)

        return callCatching { service.replaceSuccessor(newIpAddress = newIpAddress ?: "") }
            .getOrElse { return ResultData.Error(Error.ServiceNotAvailable) }
    }

}