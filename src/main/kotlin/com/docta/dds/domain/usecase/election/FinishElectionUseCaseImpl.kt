package com.docta.dds.domain.usecase.election

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.model.election.ElectionContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.HttpClient

class FinishElectionUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext,
    private val electionContext: ElectionContext
) : FinishElectionUseCase {

    override suspend fun execute(
        newLeaderId: String,
        newLeaderAddress: String,
        chatState: ChatState
    ): SimpleResult<NodeError> {
        if (nodeContext.getNodeIdString() == newLeaderId) {
            return SimpleResult.Success()
        }

        nodeContext.updateLeader(leaderId = newLeaderId, leaderAddress = newLeaderAddress)
        electionContext.reset()

        nodeContext.successorAddress?.let {
            val service: NodeService = NodeRestControllerImpl(hostname = it, client = client)
            return callCatching {
                service.finishElection(
                    newLeaderId = newLeaderId,
                    newLeaderAddress = newLeaderAddress,
                    chatState = chatState
                )
            }.getOrElse { return SimpleResult.Error(NodeError.ServiceNotAvailable) }
        }

        return SimpleResult.Success()
    }

}