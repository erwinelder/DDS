package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult

interface JoinRingUseCase {

    fun execute(nextNeighborAddress: String, prevNeighborAddress: String): SimpleResult<Error>

}