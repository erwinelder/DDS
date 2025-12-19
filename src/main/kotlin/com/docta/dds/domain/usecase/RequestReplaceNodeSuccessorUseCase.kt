package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.drpc.core.result.ResultData

interface RequestReplaceNodeSuccessorUseCase {

    suspend fun execute(targetNodeIpAddress: String, newIpAddress: String? = null): ResultData<String?, Error>

}