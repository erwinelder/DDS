package com.docta.dds.domain.usecase.chat

import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.error.ChatError
import com.docta.drpc.core.result.SimpleResult

interface BroadcastMessageUseCase {

    suspend fun execute(message: ChatMessage): SimpleResult<ChatError>

}