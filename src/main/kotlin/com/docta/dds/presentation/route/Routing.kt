package com.docta.dds.presentation.route

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.*
import io.ktor.server.plugins.origin
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        get("/health") {
            call.respondText("Is running!")
        }

        get("/join") {
            val greeterIp = System.getenv("GREETER_IP")
                ?: call.respondText("There is no one to greet me...")

            val response = HttpClient(CIO).get("http://$greeterIp:8080/greet")

            call.respondText("Response from the greeter: ${response.bodyAsText()}")
        }

        get("/greet") {
            val callerIp = call.request.origin.remoteAddress

            call.respondText("Hello, $callerIp!")
        }

    }
}
