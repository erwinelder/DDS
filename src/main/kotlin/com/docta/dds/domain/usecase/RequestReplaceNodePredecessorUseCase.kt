package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult

interface RequestReplaceNodePredecessorUseCase {

    suspend fun execute(
        targetNodeAddress: String,
        newPredecessorAddress: String,
        newPrePredecessorAddress: String
    ): SimpleResult<Error>

}