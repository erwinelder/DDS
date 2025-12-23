package com.docta.dds.presentation.service

import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.error.ChatError
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult

interface ChatService {

    context(ctx: DrpcContext)
    suspend fun getChatHistory(): ResultData<ChatState, ChatError>

    context(ctx: DrpcContext)
    suspend fun sendMessage(text: String): SimpleResult<ChatError>

    context(ctx: DrpcContext)
    suspend fun sendMessageRequest(request: ChatMessageRequest): SimpleResult<ChatError>

    context(ctx: DrpcContext)
    suspend fun broadcastMessage(message: ChatMessage): SimpleResult<ChatError>

}