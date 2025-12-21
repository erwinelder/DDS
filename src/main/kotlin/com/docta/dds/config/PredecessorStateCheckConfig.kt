package com.docta.dds.config

import com.docta.dds.domain.model.AppContext
import com.docta.dds.domain.usecase.CheckPredecessorIsAliveUseCase
import com.docta.dds.domain.usecase.RecoverFromPredecessorDeathUseCase
import com.docta.drpc.core.result.runOnError
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.ext.get

fun Application.configurePredecessorStateCheck(
    appContext: AppContext = get(),
    checkPredecessorIsAliveUseCase: CheckPredecessorIsAliveUseCase = get(),
    recoverFromPredecessorDeathUseCase: RecoverFromPredecessorDeathUseCase = get()
) {
    launch {
        while (true) {
            delay(appContext.predecessorStateCheckInterval)
            checkPredecessorIsAliveUseCase.execute().runOnError { recoverFromPredecessorDeathUseCase.execute() }
        }
    }
}