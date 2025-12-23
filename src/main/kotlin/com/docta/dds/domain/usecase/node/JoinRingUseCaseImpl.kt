package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.error.NodeError
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse
import io.ktor.client.*

class JoinRingUseCaseImpl(
    private val client: HttpClient,
    private val nodeContext: NodeContext,
    private val chatContext: ChatContext
) : JoinRingUseCase {

    override suspend fun execute(greeterIpAddress: String?): SimpleResult<NodeError> {
        if (nodeContext.isRegistered()) return SimpleResult.Error(NodeError.NodeIsAlreadyRegistered)

        if (greeterIpAddress == null) {
            nodeContext.registerNode()
            return SimpleResult.Success()
        }

        val service: NodeService = NodeRestControllerImpl(hostname = greeterIpAddress, client = client)

        val registrationState = callCatching { service.registerNode() }
            .getOrElse { return SimpleResult.Error(NodeError.RegisterFailed) }
            .getOrElse { return SimpleResult.Error(it) }

        nodeContext.registerNode(registrationState = registrationState)
        chatContext.updateChatState(state = registrationState.chatState)

        return SimpleResult.Success()
    }

}