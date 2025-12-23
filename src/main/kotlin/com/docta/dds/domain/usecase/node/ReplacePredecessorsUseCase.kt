package com.docta.dds.domain.usecase.node

import com.docta.dds.error.NodeError
import com.docta.dds.domain.model.node.NodeState
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.SimpleResult

interface ReplacePredecessorsUseCase {

    suspend fun execute(predecessors: List<String>): SimpleResult<NodeError>

    suspend fun execute(targetNodeAddress: String, predecessors: List<String>): ResultData<NodeState, NodeError>

}