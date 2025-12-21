package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.drpc.core.result.SimpleResult

interface ProclaimLeaderUseCase {

    suspend fun execute(leaderId: String, leaderAddress: String): SimpleResult<Error>

}