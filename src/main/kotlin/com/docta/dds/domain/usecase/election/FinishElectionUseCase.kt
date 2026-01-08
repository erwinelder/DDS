package com.docta.dds.domain.usecase.election

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.chat.ChatState
import com.docta.drpc.core.result.SimpleResult

interface FinishElectionUseCase {

    suspend fun execute(newLeaderId: String, newLeaderAddress: String, chatState: ChatState): SimpleResult<NodeError>

}