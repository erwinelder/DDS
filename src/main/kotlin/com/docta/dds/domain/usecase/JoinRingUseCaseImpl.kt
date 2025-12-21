package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse
import io.ktor.client.*

class JoinRingUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : JoinRingUseCase {

    override suspend fun execute(greeterIpAddress: String?): SimpleResult<Error> {
        if (nodeState.isRegistered()) return SimpleResult.Error(Error.NodeIsAlreadyRegistered)

        if (greeterIpAddress == null) {
            nodeState.registerNode()
            return SimpleResult.Success()
        }

        val service: NodeService = NodeRestControllerImpl(hostname = greeterIpAddress, client = client)

        val registrationState = callCatching { service.registerNode() }
            .getOrElse { return SimpleResult.Error(Error.ServiceNotAvailable) }
            .getOrElse { return SimpleResult.Error(it) }

        nodeState.registerNode(registrationState = registrationState)

        return SimpleResult.Success()
    }

}