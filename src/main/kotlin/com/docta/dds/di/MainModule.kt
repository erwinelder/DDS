package com.docta.dds.di

import com.docta.dds.domain.model.NodeState
import com.docta.dds.domain.usecase.RequestNodeRegistrationUseCase
import com.docta.dds.domain.usecase.RequestNodeRegistrationUseCaseImpl
import com.docta.dds.domain.usecase.RequestReplaceNodePredecessorUseCase
import com.docta.dds.domain.usecase.RequestReplaceNodePredecessorUseCaseImpl
import com.docta.dds.presentation.controller.NodeRestController
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import com.docta.dds.presentation.service.NodeServiceImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val mainModule = module {

    /* ---------- Other ---------- */

    single {
        NodeState
    }

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /* ---------- Use Cases ---------- */

    single<RequestNodeRegistrationUseCase> {
        RequestNodeRegistrationUseCaseImpl(client = get())
    }

    single<RequestReplaceNodePredecessorUseCase> {
        RequestReplaceNodePredecessorUseCaseImpl(client = get())
    }

    /* ---------- Services ---------- */

    single<NodeService> {
        NodeServiceImpl(
            nodeState = get(),
            requestNodeRegistrationUseCase = get(),
            requestReplaceNodePredecessorUseCase = get()
        )
    }

    factory<NodeRestController> { params ->
        NodeRestControllerImpl(
            hostname = params.getOrNull() ?: "",
            client = get()
        )
    }

}