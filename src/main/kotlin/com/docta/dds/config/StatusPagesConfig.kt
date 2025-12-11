package com.docta.dds.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages(
    configureCustomStatusPages: StatusPagesConfig.() -> Unit = {}
) {
    install(StatusPages) {

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }

        configureCustomStatusPages()

    }
}