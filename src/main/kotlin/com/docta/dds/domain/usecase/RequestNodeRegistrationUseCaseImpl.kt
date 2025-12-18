package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.ResultData
import io.ktor.client.*

class RequestNodeRegistrationUseCaseImpl(
    private val client: HttpClient
) : RequestNodeRegistrationUseCase {

    override suspend fun execute(greeterIpAddress: String): ResultData<RegistrationStateDto, Error> {
        val service: NodeService = NodeRestControllerImpl(hostname = greeterIpAddress, client = client)

        return callCatching { service.registerNode() }
            .getOrElse { return ResultData.Error(Error.ServiceNotAvailable) }
    }

}