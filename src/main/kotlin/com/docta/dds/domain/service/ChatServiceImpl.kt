package com.docta.dds.domain.service

import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.usecase.chat.BroadcastMessageRequestUseCase
import com.docta.dds.domain.usecase.chat.BroadcastMessageUseCase
import com.docta.dds.domain.usecase.chat.SendMessageUseCase
import com.docta.dds.domain.error.ChatError
import com.docta.dds.presentation.service.ChatService
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.onError

class ChatServiceImpl(
    private val nodeContext: NodeContext,
    private val chatContext: ChatContext,
    private val sendMessageUseCase: SendMessageUseCase,
    private val broadcastMessageRequestUseCase: BroadcastMessageRequestUseCase,
    private val broadcastMessageUseCase: BroadcastMessageUseCase
) : ChatService {

    context(ctx: DrpcContext)
    override suspend fun getChatHistory(): ResultData<ChatState, ChatError> {
        val state = chatContext.getChatState()
        return ResultData.Success(data = state)
    }

    context(ctx: DrpcContext)
    override suspend fun sendMessage(text: String): SimpleResult<ChatError> {
        val request = ChatMessageRequest(
            text = text,
            senderAddress = nodeContext.nodeAddress
        )

        return sendMessageUseCase.execute(request = request)
    }

    context(ctx: DrpcContext)
    override suspend fun sendMessageRequest(request: ChatMessageRequest): SimpleResult<ChatError> {
        if (nodeContext.isLeader) {
            broadcastMessageRequestUseCase.execute(request = request).onError { return SimpleResult.Error(it) }
        } else {
            sendMessageUseCase.execute(request = request).onError { return SimpleResult.Error(it) }
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun broadcastMessage(message: ChatMessage): SimpleResult<ChatError> {
        if (nodeContext.isLeader) return SimpleResult.Success()

        return broadcastMessageUseCase.execute(message = message)
    }

}