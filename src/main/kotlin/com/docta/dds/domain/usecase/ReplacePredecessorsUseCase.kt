package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.dds.presentation.model.NodeStateDto
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult

interface ReplacePredecessorsUseCase {

    suspend fun execute(predecessors: List<String>): SimpleResult<Error>

    suspend fun execute(targetNodeAddress: String, predecessors: List<String>): ResultData<NodeStateDto, Error>

}