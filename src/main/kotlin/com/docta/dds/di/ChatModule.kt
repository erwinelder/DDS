package com.docta.dds.di

import com.docta.dds.domain.model.chat.ChatContext
import com.docta.dds.domain.service.ChatServiceImpl
import com.docta.dds.domain.usecase.chat.BroadcastMessageRequestUseCase
import com.docta.dds.domain.usecase.chat.BroadcastMessageRequestUseCaseImpl
import com.docta.dds.domain.usecase.chat.BroadcastMessageUseCase
import com.docta.dds.domain.usecase.chat.BroadcastMessageUseCaseImpl
import com.docta.dds.domain.usecase.chat.SendMessageUseCase
import com.docta.dds.domain.usecase.chat.SendMessageUseCaseImpl
import com.docta.dds.presentation.controller.ChatRestController
import com.docta.dds.presentation.controller.ChatRestControllerImpl
import com.docta.dds.presentation.service.ChatService
import org.koin.dsl.module

val chatModule = module {

    /* ---------- Use Cases ---------- */

    single {
        ChatContext
    }

    /* ---------- Use Cases ---------- */

    single<SendMessageUseCase> {
        SendMessageUseCaseImpl(
            client = get(),
            nodeContext = get()
        )
    }

    single<BroadcastMessageRequestUseCase> {
        BroadcastMessageRequestUseCaseImpl(
            chatContext = get(),
            broadcastMessageUseCase = get()
        )
    }

    single<BroadcastMessageUseCase> {
        BroadcastMessageUseCaseImpl(
            client = get(),
            chatContext = get(),
            nodeContext = get()
        )
    }

    /* ---------- Services ---------- */

    single<ChatService> {
        ChatServiceImpl(
            nodeContext = get(),
            chatContext = get(),
            sendMessageUseCase = get(),
            broadcastMessageRequestUseCase = get(),
            broadcastMessageUseCase = get()
        )
    }
    factory<ChatRestController> { params ->
        ChatRestControllerImpl(
            hostname = params.getOrNull() ?: "",
            client = get()
        )
    }

}