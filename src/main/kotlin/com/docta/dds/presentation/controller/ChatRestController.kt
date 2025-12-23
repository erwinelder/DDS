package com.docta.dds.presentation.controller

import com.docta.dds.presentation.service.ChatService

interface ChatRestController : ChatService {

    val hostname: String

    val serviceRoute: String
        get() = "/Chat"

    val absoluteUrl: String
        get() = "http://$hostname:8080$serviceRoute"


    val getChatHistoryPath: String
        get() = "/getChatHistory"

    val sendMessagePath: String
        get() = "/sendMessage"

    val sendMessageRequestPath: String
        get() = "/sendMessageRequest"

    val broadcastMessagePath: String
        get() = "/broadcastMessage"

}