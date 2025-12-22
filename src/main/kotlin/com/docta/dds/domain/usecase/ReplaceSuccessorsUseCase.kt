package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult

interface ReplaceSuccessorsUseCase {

    suspend fun execute(successors: List<String>): SimpleResult<Error>

}