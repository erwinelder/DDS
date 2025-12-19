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

        val neighborAddress = nodeState.successorAddress

        if (neighborAddress != null) {
            val result = requestReplaceNodePredecessorUseCase.execute(
                targetNodeIpAddress = neighborAddress, newIpAddress = newNodeAddress
            )

            when (result) {
                is SimpleResult.Success -> {
                    val registrationState = RegistrationStateDto(successorIpAddress = neighborAddress)
                    nodeState.setSuccessor(address = newNodeAddress)
                    return ResultData.Success(data = registrationState)
                }
                is SimpleResult.Error -> return ResultData.Error(result.error)
            }
        } else {
            nodeState.setSuccessor(address = newNodeAddress)
            nodeState.setPredecessor(address = newNodeAddress)

            return ResultData.Success(data = RegistrationStateDto())
        }
    }

}