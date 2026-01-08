package com.docta.dds.domain.usecase.node

import com.docta.dds.data.utils.callSuspend
import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.onError
import io.ktor.client.*

class LeaveRingUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext
) : LeaveRingUseCase {

    override suspend fun execute(): SimpleResult<NodeError> {
        if (!nodeContext.isRegistered()) return SimpleResult.Error(NodeError.NodeIsNotRegisteredYet)

        val successorAddress = nodeContext.successorAddress ?: return SimpleResult.Success()
        val predecessorAddress = nodeContext.predecessorAddress ?: return SimpleResult.Success()

        val successorService: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)
        val predecessorService: NodeService = NodeRestControllerImpl(hostname = predecessorAddress, client = client)

        if (successorAddress == predecessorAddress) {
            return callSuspend { successorService.initiateLonelinessProtocol() }
                .getOrElse { return SimpleResult.Error(NodeError.InitiateLonelinessProtocolFailed) }
        }

        callSuspend { successorService.replacePredecessors(predecessors = nodeContext.getPredecessors()) }
            .getOrElse { return SimpleResult.Error(NodeError.ReplacePredecessorsFailed) }
            .onError { return SimpleResult.Error(it) }

        callSuspend { predecessorService.replaceSuccessors(successors = nodeContext.getSuccessors()) }
            .getOrElse { return SimpleResult.Error(NodeError.ReplaceSuccessorsFailed) }
            .onError { return SimpleResult.Error(it) }

        if (nodeContext.isLeader) {
            callSuspend { successorService.startElection() }
                .getOrElse { return SimpleResult.Error(NodeError.StartElectionFailed) }
        }

        return SimpleResult.Success()
    }

}