package com.docta.dds.domain.usecase.election

import com.docta.dds.domain.error.NodeError
import com.docta.drpc.core.result.SimpleResult

interface ContinueWithElectionUseCase {

    suspend fun execute(candidateId: String): SimpleResult<NodeError>

}