package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.error.NodeError
import com.docta.dds.domain.model.node.RegistrationState
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.onError

class RegisterNodeUseCaseImpl(
    private val nodeContext: NodeContext,
    private val chatContext: ChatContext,
    private val replacePredecessorsUseCase: ReplacePredecessorsUseCase,
    private val replaceSuccessorsUseCase: ReplaceSuccessorsUseCase
) : RegisterNodeUseCase {

    override suspend fun execute(newNodeAddress: String): ResultData<RegistrationState, NodeError> {
        if (!nodeContext.isRegistered()) return ResultData.Error(NodeError.NodeIsNotRegisteredYet)

        val successorAddress = nodeContext.successorAddress
        val predecessorAddress = nodeContext.predecessorAddress

        return if (successorAddress != null && predecessorAddress != null) {
            val registrationState = RegistrationState(
                leaderId = nodeContext.getLeaderIdString(),
                leaderAddress = nodeContext.getLeaderAddressString(),
                successors = nodeContext.getSuccessors().toMutableList().apply { if (size == 1) add(nodeContext.nodeAddress) },
                predecessors = listOf(nodeContext.nodeAddress),
                chatState = chatContext.getChatState()
            )

            replacePredecessorsUseCase
                .execute(targetNodeAddress = successorAddress, predecessors = listOf(newNodeAddress))
                .onError { return ResultData.Error(it) }
            replaceSuccessorsUseCase.execute(successors = listOf(newNodeAddress, successorAddress))
                .onError { return ResultData.Error(it) }

            ResultData.Success(data = registrationState)
        } else {
            nodeContext.setSuccessors(addresses = listOf(newNodeAddress))
            nodeContext.setPredecessors(addresses = listOf(newNodeAddress))

            val registrationState = RegistrationState(
                leaderId = nodeContext.getLeaderIdString(),
                leaderAddress = nodeContext.getLeaderAddressString(),
                successors = listOf(nodeContext.nodeAddress),
                predecessors = listOf(nodeContext.nodeAddress),
                chatState = chatContext.getChatState()
            )
            ResultData.Success(data = registrationState)
        }
    }

}