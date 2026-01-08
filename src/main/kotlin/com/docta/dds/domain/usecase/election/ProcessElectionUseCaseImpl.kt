package com.docta.dds.domain.usecase.election

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.election.ElectionContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.model.election.ElectionStatus
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ProcessElectionUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext,
    private val chatContext: ChatContext,
    private val electionContext: ElectionContext,
    private val continueWithElectionUseCase: ContinueWithElectionUseCase
) : ProcessElectionUseCase {

    override suspend fun execute(candidateId: String): SimpleResult<NodeError> {
        if (
            electionContext.status == ElectionStatus.Leader &&
            candidateId == nodeContext.getNodeIdString()
        ) {
            return SimpleResult.Success()
        }

        if (electionContext.status == ElectionStatus.Relay) {
            return continueWithElectionUseCase.execute(candidateId = candidateId)
        }

        if (
            candidateId == nodeContext.getNodeIdString() &&
            electionContext.status == ElectionStatus.Active &&
            !electionContext.seenHigherId
        ) {
            return proclaimAsLeader()
        }

        return if (candidateId > nodeContext.getNodeIdString()) {
            electionContext.setSeenHigherId(value = true)
            electionContext.setStatus(status = ElectionStatus.Relay)

            continueWithElectionUseCase.execute(candidateId = candidateId)
        } else {
            continueWithElectionUseCase.execute(candidateId = nodeContext.getNodeIdString())
        }
    }

    private suspend fun proclaimAsLeader(): SimpleResult<NodeError> {
        electionContext.setStatus(status = ElectionStatus.Leader)
        electionContext.reset()

        nodeContext.proclaimAsLeader()

        nodeContext.successorAddress?.let {
            val service: NodeService = NodeRestControllerImpl(hostname = it, client = client)
            return callCatching {
                service.finishElection(
                    newLeaderId = nodeContext.getNodeIdString(),
                    newLeaderAddress = nodeContext.nodeAddress,
                    chatState = chatContext.getChatState()
                )
            }.getOrElse { return SimpleResult.Error(NodeError.ServiceNotAvailable) }
        }

        return SimpleResult.Success()
    }

}