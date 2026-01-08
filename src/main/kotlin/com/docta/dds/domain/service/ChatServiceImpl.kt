package com.docta.dds.domain.service

import com.docta.dds.domain.error.ChatError
import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.chat.ChatMessage
import com.docta.dds.domain.model.chat.ChatMessageRequest
import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.domain.model.core.AppContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.usecase.chat.BroadcastMessageRequestUseCase
import com.docta.dds.domain.usecase.chat.BroadcastMessageUseCase
import com.docta.dds.domain.usecase.chat.SendMessageUseCase
import com.docta.dds.domain.usecase.node.RecoverFromSuccessorDeathUseCase
import com.docta.dds.presentation.service.ChatService
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.runOnError

class ChatServiceImpl(
    private val nodeContext: NodeContext,
    private val chatContext: ChatContext,
    private val sendMessageUseCase: SendMessageUseCase,
    private val broadcastMessageRequestUseCase: BroadcastMessageRequestUseCase,
    private val broadcastMessageUseCase: BroadcastMessageUseCase,
    private val recoverFromSuccessorDeathUseCase: RecoverFromSuccessorDeathUseCase
) : ChatService {

    context(ctx: DrpcContext)
    override suspend fun getChatHistory(): ResultData<ChatState, ChatError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): Getting chat history.")

        val state = chatContext.getChatState()
        return ResultData.Success(data = state)
    }

    context(ctx: DrpcContext)
    override suspend fun sendMessage(text: String): SimpleResult<ChatError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): Sending message '$text'.")

        val request = ChatMessageRequest(
            text = text,
            senderAddress = nodeContext.nodeAddress
        )

        sendMessageUseCase.execute(request = request).runOnError { error ->
            return if (error is ChatError.ServiceNotAvailable) {
                recoverFromSuccessorDeathUseCase.execute()
                sendMessageUseCase.execute(request = request)
            } else {
                SimpleResult.Error(error)
            }
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun sendMessageRequest(request: ChatMessageRequest): SimpleResult<ChatError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): Received message request from ${request.senderAddress}.")

        if (nodeContext.isLeader) {
            broadcastMessageRequestUseCase.execute(request = request).runOnError { error ->
                return if (error is ChatError.ServiceNotAvailable) {
                    recoverFromSuccessorDeathUseCase.execute()
                    broadcastMessageRequestUseCase.execute(request = request)
                } else {
                    SimpleResult.Error(error)
                }
            }
        } else {
            sendMessageUseCase.execute(request = request).runOnError { error ->
                return if (error is ChatError.ServiceNotAvailable) {
                    recoverFromSuccessorDeathUseCase.execute()
                    sendMessageUseCase.execute(request = request)
                } else {
                    SimpleResult.Error(error)
                }
            }
        }

        return SimpleResult.Success()
    }

    context(ctx: DrpcContext)
    override suspend fun broadcastMessage(message: ChatMessage): SimpleResult<ChatError> {
        AppContext.log(message = "Node (${nodeContext.nodeAddress}): Broadcasting message ID ${message.messageId} from ${message.senderAddress}.")

        if (nodeContext.isLeader) return SimpleResult.Success()

        broadcastMessageUseCase.execute(message = message).runOnError { error ->
            return if (error is ChatError.ServiceNotAvailable) {
                recoverFromSuccessorDeathUseCase.execute()
                broadcastMessageUseCase.execute(message = message)
            } else {
                SimpleResult.Error(error)
            }
        }

        return SimpleResult.Success()
    }

}