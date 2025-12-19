package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.core.result.getOrElse

class RecoverFromPredecessorDeathUseCaseImpl(
    private val nodeState: NodeState,
    private val requestReplaceNodeSuccessorUseCase: RequestReplaceNodeSuccessorUseCase
) : RecoverFromPredecessorDeathUseCase {

    override suspend fun execute(): SimpleResult<Error> {
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
            nodeState.setIsLeader(isLeader = true)
        }

        return SimpleResult.Success()
    }

}