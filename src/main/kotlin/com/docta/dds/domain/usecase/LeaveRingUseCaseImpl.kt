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

        val successorAddress = nodeState.successorAddress ?: return SimpleResult.Success()
        val predecessorAddress = nodeState.predecessorAddress ?: return SimpleResult.Success()

        val successorService: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)
        val predecessorService: NodeService = NodeRestControllerImpl(hostname = predecessorAddress, client = client)

        if (successorAddress == predecessorAddress) {
            return callCatching { successorService.initiateLonelinessProtocol() }
                .getOrElse { return SimpleResult.Error(Error.InitiateLonelinessProtocolFailed) }
        }

        callCatching { successorService.replacePredecessors(predecessors = nodeState.getPredecessors()) }
            .getOrElse { return SimpleResult.Error(Error.ReplacePredecessorsFailed) }
            .onError { return SimpleResult.Error(it) }

        callCatching { predecessorService.replaceSuccessors(successors = nodeState.getSuccessors()) }
            .getOrElse { return SimpleResult.Error(Error.ReplaceSuccessorsFailed) }
            .onError { return SimpleResult.Error(it) }

        callCatching { successorService.proclaimLeader(leaderId = "", leaderAddress = "") }

        return SimpleResult.Success()
    }

}