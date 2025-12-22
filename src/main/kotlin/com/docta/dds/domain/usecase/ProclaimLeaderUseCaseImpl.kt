package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.HttpClient

class ProclaimLeaderUseCaseImpl(
    private val client: HttpClient,
    private val nodeState: NodeState
) : ProclaimLeaderUseCase {

    override suspend fun execute(
        leaderId: String,
        leaderAddress: String
    ): SimpleResult<Error> {
        nodeState.updateLeader(leaderId = leaderId, leaderAddress = leaderAddress)

        val successorAddress = nodeState.successorAddress ?: return SimpleResult.Error(Error.RingConsistsOnlyOfOneNode)

        val service: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)
        return callCatching { service.proclaimLeader(leaderId = leaderId, leaderAddress = leaderAddress) }
            .getOrElse { return SimpleResult.Error(Error.ProclaimLeaderFailed) }
    }

}