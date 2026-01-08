package com.docta.dds.di

import com.docta.dds.domain.model.election.ElectionContext
import com.docta.dds.domain.model.node.NodeContext
import com.docta.dds.domain.service.NodeServiceImpl
import com.docta.dds.domain.usecase.election.ContinueWithElectionUseCase
import com.docta.dds.domain.usecase.election.ContinueWithElectionUseCaseImpl
import com.docta.dds.domain.usecase.election.FinishElectionUseCase
import com.docta.dds.domain.usecase.election.FinishElectionUseCaseImpl
import com.docta.dds.domain.usecase.election.ProcessElectionUseCase
import com.docta.dds.domain.usecase.election.ProcessElectionUseCaseImpl
import com.docta.dds.domain.usecase.election.StartElectionUseCase
import com.docta.dds.domain.usecase.election.StartElectionUseCaseImpl
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

    single {
        ElectionContext
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
            nodeContext = get()
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
            replaceSuccessorsUseCase = get(),
            replacePredecessorsUseCase = get(),
            startElectionUseCase = get(),
            initiateLonelinessProtocolUseCase = get()
        )
    }

    single<StartElectionUseCase> {
        StartElectionUseCaseImpl(
            nodeContext = get(),
            electionContext = get(),
            continueWithElectionUseCase = get()
        )
    }

    single<ContinueWithElectionUseCase> {
        ContinueWithElectionUseCaseImpl(
            client = get(),
            nodeContext = get()
        )
    }

    single<ProcessElectionUseCase> {
        ProcessElectionUseCaseImpl(
            client = get(),
            nodeContext = get(),
            chatContext = get(),
            electionContext = get(),
            continueWithElectionUseCase = get()
        )
    }

    single<FinishElectionUseCase> {
        FinishElectionUseCaseImpl(
            client = get(),
            nodeContext = get(),
            electionContext = get()
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
            startElectionUseCase = get(),
            processElectionUseCase = get(),
            finishElectionUseCase = get(),
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