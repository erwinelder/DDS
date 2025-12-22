package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.onError

class RegisterNodeUseCaseImpl(
    private val nodeState: NodeState,
    private val replacePredecessorsUseCase: ReplacePredecessorsUseCase,
    private val replaceSuccessorsUseCase: ReplaceSuccessorsUseCase
) : RegisterNodeUseCase {

    override suspend fun execute(newNodeAddress: String): ResultData<RegistrationStateDto, Error> {
        if (!nodeState.isRegistered()) return ResultData.Error(Error.NodeIsNotRegisteredYet)

        val successorAddress = nodeState.successorAddress
        val predecessorAddress = nodeState.predecessorAddress

        return if (successorAddress != null && predecessorAddress != null) {
            val registrationState = RegistrationStateDto(
                leaderId = nodeState.getLeaderIdString(),
                leaderAddress = nodeState.getLeaderAddressString(),
                successors = nodeState.getSuccessors().toMutableList().apply { if (size == 1) add(nodeState.nodeAddress) },
                predecessors = listOf(nodeState.nodeAddress)
            )

            replacePredecessorsUseCase
                .execute(targetNodeAddress = successorAddress, predecessors = listOf(newNodeAddress))
                .onError { return ResultData.Error(it) }
            replaceSuccessorsUseCase.execute(successors = listOf(newNodeAddress, successorAddress))
                .onError { return ResultData.Error(it) }

            ResultData.Success(data = registrationState)
        } else {
            nodeState.setSuccessors(addresses = listOf(newNodeAddress))
            nodeState.setPredecessors(addresses = listOf(newNodeAddress))

            val registrationState = RegistrationStateDto(
                leaderId = nodeState.getLeaderIdString(),
                leaderAddress = nodeState.getLeaderAddressString(),
                successors = listOf(nodeState.nodeAddress),
                predecessors = listOf(nodeState.nodeAddress)
            )
            ResultData.Success(data = registrationState)
        }
    }

}