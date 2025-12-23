package com.docta.dds.domain.usecase.chat

import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.error.ChatError
import com.docta.dds.presentation.controller.ChatRestControllerImpl
import com.docta.dds.presentation.service.ChatService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.HttpClient

class BroadcastMessageUseCaseImpl(
    private val client: HttpClient,
    private val chatContext: ChatContext,
    private val nodeContext: NodeContext
) : BroadcastMessageUseCase {

    override suspend fun execute(message: ChatMessage): SimpleResult<ChatError> {
        chatContext.processNewMessage(message = message)

        val successorAddress = nodeContext.successorAddress ?: return SimpleResult.Success()
        val service: ChatService = ChatRestControllerImpl(hostname = successorAddress, client = client)

        return callCatching { service.broadcastMessage(message = message) }
            .getOrElse { return SimpleResult.Error(ChatError.ServiceNotAvailable) }
    }

}