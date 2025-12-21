package com.docta.dds.domain.usecase

import com.docta.dds.domain.model.NodeState

class InitiateLonelinessProtocolUseCaseImpl(
    private val nodeState: NodeState
) : InitiateLonelinessProtocolUseCase {

    override fun execute() {
        nodeState.resetAllNeighbors()
        nodeState.proclaimAsLeader()
    }

}