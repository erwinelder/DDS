package com.docta.dds.domain.usecase.node

import com.docta.dds.error.NodeError
import com.docta.drpc.core.result.SimpleResult

interface ReplaceSuccessorsUseCase {

    suspend fun execute(successors: List<String>): SimpleResult<NodeError>

}