package com.docta.dds.domain.usecase

import com.docta.dds.error.Error
import com.docta.dds.presentation.model.RegistrationStateDto
import com.docta.drpc.core.result.ResultData

interface RequestNodeRegistrationUseCase {

    suspend fun execute(greeterIpAddress: String): ResultData<RegistrationStateDto, Error>

}