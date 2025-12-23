package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.model.node.NodeContext

class InitiateLonelinessProtocolUseCaseImpl(
    private val nodeContext: NodeContext
) : InitiateLonelinessProtocolUseCase {

    override fun execute() {
        nodeContext.removeAllNeighbors()
        nodeContext.proclaimAsLeader()
    }

}