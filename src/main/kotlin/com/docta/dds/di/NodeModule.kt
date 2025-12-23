package com.docta.dds.di

import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.service.NodeServiceImpl
import com.docta.dds.domain.usecase.node.*
import com.docta.dds.presentation.controller.NodeRestController
import com.docta.dds.presentation.controller.NodeRestControllerImpl
import com.docta.dds.presentation.service.NodeService
import org.koin.dsl.module

val nodeModule = module {

    /* ---------- Other ---------- */

    single {
        NodeContext
    }

    /* ---------- Use Cases ---------- */

    single<JoinRingUseCase> {
        JoinRingUseCaseImpl(
            client = get(),
            nodeContext = get(),
            chatContext = get()
        )
    }

    single<RegisterNodeUseCase> {
        RegisterNodeUseCaseImpl(
            nodeContext = get(),
            chatContext = get(),
            replacePredecessorsUseCase = get(),
            replaceSuccessorsUseCase = get()
        )
    }

    single<LeaveRingUseCase> {
        LeaveRingUseCaseImpl(
            client = get(),
            nodeContext = get(),
            chatContext = get()
        )
    }

    single<ReplaceSuccessorsUseCase> {
        ReplaceSuccessorsUseCaseImpl(client = get(), nodeContext = get())
    }
    single<ReplacePredecessorsUseCase> {
        ReplacePredecessorsUseCaseImpl(client = get(), nodeContext = get())
    }

    single<CheckSuccessorIsAliveUseCase> {
        CheckSuccessorIsAliveUseCaseImpl(
            client = get(),
            nodeContext = get()
        )
    }
    single<RecoverFromSuccessorDeathUseCase> {
        RecoverFromSuccessorDeathUseCaseImpl(
            nodeContext = get(),
            chatContext = get(),
            replaceSuccessorsUseCase = get(),
            replacePredecessorsUseCase = get(),
            proclaimLeaderUseCase = get(),
            initiateLonelinessProtocolUseCase = get()
        )
    }

    single<ProclaimLeaderUseCase> {
        ProclaimLeaderUseCaseImpl(
            client = get(),
            nodeContext = get(),
            chatContext = get()
        )
    }

    single<InitiateLonelinessProtocolUseCase> {
        InitiateLonelinessProtocolUseCaseImpl(nodeContext = get())
    }

    /* ---------- Services ---------- */

    single<NodeService> {
        NodeServiceImpl(
            nodeContext = get(),
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