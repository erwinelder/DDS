package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.onError
import io.ktor.client.*

class LeaveRingUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : LeaveRingUseCase {

    override suspend fun execute(): SimpleResult<Error> {
        if (!nodeState.isRegistered()) return SimpleResult.Error(Error.NodeIsNotRegisteredYet)
        val successorAddress = nodeState.successorAddress ?: return SimpleResult.Error(Error.NodeIsNotRegisteredYet)
        val predecessorAddress = nodeState.predecessorAddress ?: return SimpleResult.Error(Error.NodeIsNotRegisteredYet)
        val prePredecessorAddress = nodeState.prePredecessorAddress

        val successorService: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)

        if (successorAddress == predecessorAddress || prePredecessorAddress == null) {
            return callCatching { successorService.initiateLonelinessProtocol() }
                .getOrElse { return SimpleResult.Error(Error.ServiceNotAvailable) }
        }

        callCatching {
            successorService.replacePredecessor(
                newPredecessorAddress = predecessorAddress,
                newPrePredecessorAddress = prePredecessorAddress
            )
        }
            .getOrElse { return SimpleResult.Error(Error.ServiceNotAvailable) }
            .onError { return SimpleResult.Error(it) }

        val predecessorService: NodeService = NodeRestControllerImpl(hostname = predecessorAddress, client = client)

        callCatching { predecessorService.replaceSuccessor(newIpAddress = successorAddress) }
            .getOrElse { return SimpleResult.Error(Error.ServiceNotAvailable) }
            .onError { return SimpleResult.Error(it) }

        callCatching { successorService.proclaimLeader(leaderId = "", leaderAddress = "") }

        return SimpleResult.Success()
    }

}