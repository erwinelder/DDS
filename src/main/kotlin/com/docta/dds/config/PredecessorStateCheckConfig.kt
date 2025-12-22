package com.docta.dds.config

import com.docta.dds.domain.model.AppContext
import com.docta.dds.domain.usecase.CheckSuccessorIsAliveUseCase
import com.docta.dds.domain.usecase.RecoverFromSuccessorDeathUseCase
import com.docta.drpc.core.result.runOnError
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get

fun Application.configureSuccessorStateCheck(
    appContext: AppContext = get(),
    checkSuccessorIsAliveUseCase: CheckSuccessorIsAliveUseCase = get(),
    recoverFromSuccessorDeathUseCase: RecoverFromSuccessorDeathUseCase = get()
) {
    launch {
        while (true) {
            delay(appContext.successorStateCheckInterval)
            checkSuccessorIsAliveUseCase.execute().runOnError { recoverFromSuccessorDeathUseCase.execute() }
        }
    }
}