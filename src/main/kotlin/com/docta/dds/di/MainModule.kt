package com.docta.dds.di

import com.docta.dds.domain.model.AppContext
import com.docta.dds.domain.model.NodeState
import com.docta.dds.domain.usecase.CheckSuccessorIsAliveUseCase
import com.docta.dds.domain.usecase.CheckSuccessorIsAliveUseCaseImpl
import com.docta.dds.domain.usecase.InitiateLonelinessProtocolUseCase
import com.docta.dds.domain.usecase.InitiateLonelinessProtocolUseCaseImpl
import com.docta.dds.domain.usecase.JoinRingUseCase
import com.docta.dds.domain.usecase.JoinRingUseCaseImpl
import com.docta.dds.domain.usecase.LeaveRingUseCase
import com.docta.dds.domain.usecase.LeaveRingUseCaseImpl
import com.docta.dds.domain.usecase.ProclaimLeaderUseCase
import com.docta.dds.domain.usecase.ProclaimLeaderUseCaseImpl
import com.docta.dds.domain.usecase.RecoverFromSuccessorDeathUseCase
import com.docta.dds.domain.usecase.RecoverFromSuccessorDeathUseCaseImpl
import com.docta.dds.domain.usecase.RegisterNodeUseCase
import com.docta.dds.domain.usecase.RegisterNodeUseCaseImpl
import com.docta.dds.domain.usecase.ReplacePredecessorsUseCase
import com.docta.dds.domain.usecase.ReplacePredecessorsUseCaseImpl
import com.docta.dds.domain.usecase.ReplaceSuccessorsUseCase
import com.docta.dds.domain.usecase.ReplaceSuccessorsUseCaseImpl
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
        AppContext
    }

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

    single<JoinRingUseCase> {
        JoinRingUseCaseImpl(
            client = get(),
            nodeState = get()
        )
    }

    single<RegisterNodeUseCase> {
        RegisterNodeUseCaseImpl(
            nodeState = get(),
            replacePredecessorsUseCase = get(),
            replaceSuccessorsUseCase = get()
        )
    }

    single<LeaveRingUseCase> {
        LeaveRingUseCaseImpl(
            client = get(),
            nodeState = get()
        )
    }

    single<ReplaceSuccessorsUseCase> {
        ReplaceSuccessorsUseCaseImpl(client = get(), nodeState = get())
    }
    single<ReplacePredecessorsUseCase> {
        ReplacePredecessorsUseCaseImpl(client = get(), nodeState = get())
    }

    single<CheckSuccessorIsAliveUseCase> {
        CheckSuccessorIsAliveUseCaseImpl(
            client = get(),
            nodeState = get()
        )
    }
    single<RecoverFromSuccessorDeathUseCase> {
        RecoverFromSuccessorDeathUseCaseImpl(
            nodeState = get(),
            replaceSuccessorsUseCase = get(),
            replacePredecessorsUseCase = get(),
            proclaimLeaderUseCase = get(),
            initiateLonelinessProtocolUseCase = get()
        )
    }

    single<ProclaimLeaderUseCase> {
        ProclaimLeaderUseCaseImpl(
            client = get(),
            nodeState = get()
        )
    }

    single<InitiateLonelinessProtocolUseCase> {
        InitiateLonelinessProtocolUseCaseImpl(nodeState = get())
    }

    /* ---------- Services ---------- */

    single<NodeService> {
        NodeServiceImpl(
            nodeState = get(),
            joinRingUseCase = get(),
            registerNodeUseCase = get(),
            leaveRingUseCase = get(),
            proclaimLeaderUseCase = get(),
            initiateLonelinessProtocolUseCase = get(),
            replaceSuccessorsUseCase = get(),
            replacePredecessorsUseCase = get()
        )
    }

    factory<NodeRestController> { params ->
        NodeRestControllerImpl(
            hostname = params.getOrNull() ?: "",
            client = get()
        )
    }

}