package com.docta.dds.presentation.route

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    routing {
        configureNodeRouting(
            restController = this@configureRouting.get(),
            service = this@configureRouting.get()
        )
        configureChatRouting(
            restController = this@configureRouting.get(),
            service = this@configureRouting.get()
        )
    }
}
