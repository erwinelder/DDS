package com.docta.dds.domain.usecase.chat

import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.domain.error.ChatError
import com.docta.drpc.core.result.SimpleResult

class BroadcastMessageRequestUseCaseImpl(
    private val chatContext: ChatContext,
    private val broadcastMessageUseCase: BroadcastMessageUseCase
) : BroadcastMessageRequestUseCase {

    override suspend fun execute(request: ChatMessageRequest): SimpleResult<ChatError> {
        val nextSeq = chatContext.getNextSeq()

        val message = ChatMessage(
            messageId = nextSeq,
            message = request.text,
            senderAddress = request.senderAddress
        )

        return broadcastMessageUseCase.execute(message = message)
    }

}