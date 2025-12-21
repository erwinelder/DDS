package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState
import com.docta.dds.error.Error
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult

class RegisterNodeUseCaseImpl(
    private val nodeState: NodeState,
    private val requestReplaceNodePredecessorUseCase: RequestReplaceNodePredecessorUseCase,
) : RegisterNodeUseCase {

    override suspend fun execute(newNodeAddress: String): ResultData<RegistrationStateDto, Error> {
        if (!nodeState.isRegistered()) return ResultData.Error(Error.NodeIsNotRegisteredYet)

        val successorAddress = nodeState.successorAddress

        return if (successorAddress != null) {
            val result = requestReplaceNodePredecessorUseCase.execute(
                targetNodeAddress = successorAddress,
                newPredecessorAddress = newNodeAddress,
                newPrePredecessorAddress = nodeState.nodeAddress
            )

            when (result) {
                is SimpleResult.Success -> {
                    val registrationState = RegistrationStateDto(
                        leaderId = nodeState.getLeaderIdString(),
                        leaderAddress = nodeState.getLeaderAddressString(),
                        successorAddress = successorAddress,
                        predecessorAddress = nodeState.nodeAddress,
                        prePredecessorAddress = nodeState.predecessorAddress
                    )
                    nodeState.setSuccessor(address = newNodeAddress)
                    nodeState.setPrePredecessor(address = newNodeAddress)

                    ResultData.Success(data = registrationState)
                }
                is SimpleResult.Error -> ResultData.Error(result.error)
            }
        } else {
            nodeState.setSuccessor(address = newNodeAddress)
            nodeState.setPredecessor(address = newNodeAddress)

            val registrationState = RegistrationStateDto(
                leaderId = nodeState.getLeaderIdString(),
                leaderAddress = nodeState.getLeaderAddressString(),
                successorAddress = nodeState.nodeAddress,
                predecessorAddress = nodeState.nodeAddress,
                prePredecessorAddress = null
            )
            ResultData.Success(data = registrationState)
        }
    }

}