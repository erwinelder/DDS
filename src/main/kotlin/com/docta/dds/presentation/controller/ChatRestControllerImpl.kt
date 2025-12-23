package com.docta.dds.presentation.controller

import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.error.ChatError
import com.docta.drpc.core.network.asCallParameter
import com.docta.drpc.core.network.client.callPost
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import io.ktor.client.HttpClient

class ChatRestControllerImpl(
    override val hostname: String,
    private val client: HttpClient
) : ChatRestController {

    context(ctx: DrpcContext)
    override suspend fun getChatHistory(): ResultData<ChatState, ChatError> = client.callPost(
        url = absoluteUrl + getChatHistoryPath
    )

    context(ctx: DrpcContext)
    override suspend fun sendMessage(
        text: String
    ): SimpleResult<ChatError> = client.callPost(
        url = absoluteUrl + sendMessagePath,
        text.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun sendMessageRequest(
        request: ChatMessageRequest
    ): SimpleResult<ChatError> = client.callPost(
        url = absoluteUrl + sendMessageRequestPath,
        request.asCallParameter()
    )

    context(ctx: DrpcContext)
    override suspend fun broadcastMessage(
        message: ChatMessage
    ): SimpleResult<ChatError> = client.callPost(
        url = absoluteUrl + broadcastMessagePath,
        message.asCallParameter()
    )

}