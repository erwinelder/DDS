package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse
import com.docta.drpc.core.result.onError

class RecoverFromPredecessorDeathUseCaseImpl(
    private val nodeState: NodeState,
    private val requestReplaceNodeSuccessorUseCase: RequestReplaceNodeSuccessorUseCase,
    private val proclaimLeaderUseCase: ProclaimLeaderUseCase
) : RecoverFromPredecessorDeathUseCase {

    override suspend fun execute(): SimpleResult<Error> {
        val predecessorAddress = nodeState.predecessorAddress ?: return SimpleResult.Success()
        val prePredecessorAddress = nodeState.prePredecessorAddress

        if (prePredecessorAddress != null) {
            val newPrePredecessorAddress = requestReplaceNodeSuccessorUseCase
                .execute(targetNodeIpAddress = prePredecessorAddress)
                .getOrElse { return SimpleResult.Error(it) }

            nodeState.setPredecessor(address = prePredecessorAddress)
            if (nodeState.successorAddress != prePredecessorAddress) {
                nodeState.setPrePredecessor(address = newPrePredecessorAddress)
            } else {
                nodeState.setPrePredecessor(address = null)
            }
        } else {
            nodeState.resetAllNeighbors()
            nodeState.proclaimAsLeader()
        }

        if (predecessorAddress == nodeState.leaderAddress) {
            proclaimLeaderUseCase
                .execute(leaderId = nodeState.getNodeIdString(), leaderAddress = nodeState.nodeAddress)
                .onError { return SimpleResult.Error(it) }
        }

        return SimpleResult.Success()
    }

}