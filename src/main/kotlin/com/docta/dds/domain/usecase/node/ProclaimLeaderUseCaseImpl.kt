package com.docta.dds.domain.usecase.node

import com.docta.dds.data.utils.callSuspend
import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class ProclaimLeaderUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext,
    private val chatContext: ChatContext
) : ProclaimLeaderUseCase {

    override suspend fun execute(
        leaderId: String,
        leaderAddress: String,
        chatState: ChatState
    ): SimpleResult<NodeError> {
        nodeContext.updateLeader(leaderId = leaderId, leaderAddress = leaderAddress)

        val chatState = if (chatState.seq > chatContext.seq) {
            chatContext.updateChatState(state = chatState)
            chatState
        } else {
            chatContext.getChatState()
        }

        val successorAddress = nodeContext.successorAddress ?: return SimpleResult.Error(NodeError.RingConsistsOnlyOfOneNode)
        val service: NodeService = NodeRestControllerImpl(hostname = successorAddress, client = client)

        return callSuspend {
            service.proclaimLeader(leaderId = leaderId, leaderAddress = leaderAddress, chatState = chatState)
        }.getOrElse { return SimpleResult.Error(NodeError.ProclaimLeaderFailed) }
    }

}