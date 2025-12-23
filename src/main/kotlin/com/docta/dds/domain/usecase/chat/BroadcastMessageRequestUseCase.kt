package com.docta.dds.domain.usecase.chat

import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.error.ChatError
import com.docta.drpc.core.result.SimpleResult

interface BroadcastMessageRequestUseCase {

    suspend fun execute(request: ChatMessageRequest): SimpleResult<ChatError>

}