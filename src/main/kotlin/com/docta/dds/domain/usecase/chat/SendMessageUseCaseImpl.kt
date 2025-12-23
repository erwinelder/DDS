package com.docta.dds.domain.usecase.chat

import com.docta.dds.data.utils.callSuspend
import com.docta.dds.domain.error.ChatError
import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.presentation.controller.ChatRestControllerImpl
import com.docta.dds.presentation.service.ChatService
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.*

class SendMessageUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext
) : SendMessageUseCase {

    override suspend fun execute(request: ChatMessageRequest): SimpleResult<ChatError> {
        val successorAddress = nodeContext.successorAddress ?: return SimpleResult.Success()
        val service: ChatService = ChatRestControllerImpl(hostname = successorAddress, client = client)

        return callSuspend { service.sendMessageRequest(request = request) }
            .getOrElse { return SimpleResult.Error(ChatError.ServiceNotAvailable) }
    }

}