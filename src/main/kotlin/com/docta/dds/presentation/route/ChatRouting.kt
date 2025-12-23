package com.docta.dds.presentation.route

import com.docta.dds.presentation.controller.ChatRestController
import com.docta.dds.presentation.service.ChatService
import com.docta.drpc.core.network.server.processPostRoute
import io.ktor.server.routing.*

fun Routing.configureChatRouting(
    restController: ChatRestController,
    service: ChatService
) {
    route(restController.serviceRoute) {

        processPostRoute(restController.getChatHistoryPath) {
            service.getChatHistory()
        }

        processPostRoute(restController.sendMessagePath) {
            service.sendMessage(text = get(0))
        }

        processPostRoute(restController.sendMessageRequestPath) {
            service.sendMessageRequest(request = get(0))
        }

        processPostRoute(restController.broadcastMessagePath) {
            service.broadcastMessage(message = get(0))
        }

    }
}