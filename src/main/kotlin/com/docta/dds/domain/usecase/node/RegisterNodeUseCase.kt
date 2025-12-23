package com.docta.dds.domain.usecase.node

import com.docta.dds.domain.error.NodeError
import com.docta.dds.domain.model.node.RegistrationState
import com.docta.drpc.core.result.ResultData

interface RegisterNodeUseCase {

    suspend fun execute(newNodeAddress: String): ResultData<RegistrationState, NodeError>

}