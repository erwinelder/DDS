package com.docta.dds.data.utils

import com.docta.dds.domain.model.core.AppContext
import com.docta.drpc.core.network.context.EmptyContext
import com.docta.drpc.core.network.context.callCatching
import kotlinx.coroutines.delay


suspend inline fun <R> callSuspend(block: EmptyContext.() -> R): Result<R> {
    delay(AppContext.messageDelay)
    return callCatching(block = block)
}