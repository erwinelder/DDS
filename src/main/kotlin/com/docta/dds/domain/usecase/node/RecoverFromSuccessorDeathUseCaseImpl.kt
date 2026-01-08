package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.usecase.election.StartElectionUseCase
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse
import com.docta.drpc.core.result.onError

class RecoverFromSuccessorDeathUseCaseImpl(
    private val nodeContext: NodeContext,
    private val replaceSuccessorsUseCase: ReplaceSuccessorsUseCase,
    private val replacePredecessorsUseCase: ReplacePredecessorsUseCase,
    private val initiateLonelinessProtocolUseCase: InitiateLonelinessProtocolUseCase,
    private val startElectionUseCase: StartElectionUseCase
) : RecoverFromSuccessorDeathUseCase {

    override suspend fun execute(): SimpleResult<NodeError> {
        val grandSuccessorAddress = nodeContext.grandSuccessorAddress
        val successorAddress = nodeContext.successorAddress ?: return SimpleResult.Success()

        if (grandSuccessorAddress != null) {
            val grandSuccessorState = replacePredecessorsUseCase
                .execute(targetNodeAddress = grandSuccessorAddress, predecessors = listOf(nodeContext.nodeAddress))
                .getOrElse { return SimpleResult.Error(it) }

            replaceSuccessorsUseCase
                .execute(successors = listOfNotNull(grandSuccessorAddress, grandSuccessorState.successorAddress))
                .onError { return SimpleResult.Error(it) }
        } else {
            initiateLonelinessProtocolUseCase.execute()
            return SimpleResult.Success()
        }

        if (successorAddress == nodeContext.leaderAddress) {
            startElectionUseCase.execute().onError { return SimpleResult.Error(it) }
        }

        return SimpleResult.Success()
    }

}