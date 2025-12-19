package com.docta.dds

import com.docta.dds.config.configureDI
import com.docta.dds.config.configureHTTP
import com.docta.dds.config.configurePredecessorStateCheck
import com.docta.dds.config.configureSerialization
import com.docta.dds.config.configureStatusPages
import com.docta.dds.di.mainModule
import com.docta.dds.presentation.route.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.get

fun main() {
    embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureStatusPages()
    configureDI(mainModule)
    configureRouting(
        restController = get(),
        service = get()
    )
    configurePredecessorStateCheck()
}
