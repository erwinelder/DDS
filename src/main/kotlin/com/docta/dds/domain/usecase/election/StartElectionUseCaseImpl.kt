package com.docta.dds.domain.usecase.election

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.election.ElectionContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.drpc.core.result.SimpleResult

class StartElectionUseCaseImpl(
    private val nodeContext: NodeContext,
    private val electionContext: ElectionContext,
    private val continueWithElectionUseCase: ContinueWithElectionUseCase
) : StartElectionUseCase {

    override suspend fun execute(): SimpleResult<NodeError> {
        electionContext.reset()
        return continueWithElectionUseCase.execute(candidateId = nodeContext.getNodeIdString())
    }

}