package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.chat.ChatState
import com.docta.dds.error.NodeError
import com.docta.drpc.core.result.SimpleResult

interface ProclaimLeaderUseCase {

    suspend fun execute(leaderId: String, leaderAddress: String, chatState: ChatState): SimpleResult<NodeError>

}