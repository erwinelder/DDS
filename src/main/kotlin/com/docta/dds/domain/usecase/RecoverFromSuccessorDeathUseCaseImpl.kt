package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse
import com.docta.drpc.core.result.onError

class RecoverFromSuccessorDeathUseCaseImpl(
    private val nodeState: NodeState,
    private val replaceSuccessorsUseCase: ReplaceSuccessorsUseCase,
    private val replacePredecessorsUseCase: ReplacePredecessorsUseCase,
    private val initiateLonelinessProtocolUseCase: InitiateLonelinessProtocolUseCase,
    private val proclaimLeaderUseCase: ProclaimLeaderUseCase
) : RecoverFromSuccessorDeathUseCase {

    override suspend fun execute(): SimpleResult<Error> {
        val grandSuccessorAddress = nodeState.grandSuccessorAddress
        val successorAddress = nodeState.successorAddress ?: return SimpleResult.Success()

        if (grandSuccessorAddress != null) {
            val grandSuccessorState = replacePredecessorsUseCase
                .execute(targetNodeAddress = grandSuccessorAddress, predecessors = listOf(nodeState.nodeAddress))
                .getOrElse { return SimpleResult.Error(it) }

            replaceSuccessorsUseCase
                .execute(successors = listOfNotNull(grandSuccessorAddress, grandSuccessorState.successorAddress))
                .onError { return SimpleResult.Error(it) }
        } else {
            initiateLonelinessProtocolUseCase.execute()
            return SimpleResult.Success()
        }

        if (successorAddress == nodeState.leaderAddress) {
            proclaimLeaderUseCase
                .execute(leaderId = nodeState.getNodeIdString(), leaderAddress = nodeState.nodeAddress)
                .onError { return SimpleResult.Error(it) }
        }

        return SimpleResult.Success()
    }

}