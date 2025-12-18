package com.docta.dds.presentation.route

import com.docta.dds.domain.model.NodeState
import com.docta.dds.presentation.model.RegistrationStateDto
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    routing {

        get("/health") {
            val nodeState = this@configureRouting.get<NodeState>()

            call.respondText("""
                Is running! Node state:
                - Node ID: ${nodeState.getNodeId()} (is ${if (nodeState.isRegistered()) "" else "not "}registered)
                - Is Leader: ${nodeState.isLeader}
                - Successor Address: ${nodeState.successorAddress}
                - Predecessor Address: ${nodeState.predecessorAddress}
            """.trimIndent())
        }

        post("/join") {
            val greeterIp = System.getenv("GREETER_IP")
            val nodeState = this@configureRouting.get<NodeState>()

            if (greeterIp == null) {
                nodeState.registerNode()
                return@post call.respond(HttpStatusCode.OK)
            }

            val response = HttpClient(CIO).post("http://$greeterIp:8080/register-node")
            when (response.status) {
                HttpStatusCode.OK -> {
                    val registrationState = Json.decodeFromString<RegistrationStateDto>(response.bodyAsText())
                    nodeState.registerNode(
                        registrationState = registrationState.replaceNeighborIpAddressesIfNull(newAddress = greeterIp)
                    )

                    call.respond(HttpStatusCode.OK)
                }
                HttpStatusCode.ExpectationFailed -> {
                    call.respond(HttpStatusCode.ExpectationFailed, "Greeter node ($greeterIp) is not registered yet.")
                }
                else -> {
                    println("Failed to join the network via greeter node ($greeterIp). Error: ${response.bodyAsText()}")
                    call.respond(HttpStatusCode.InternalServerError, "Failed to join the network.")
                }
            }
        }

        post("/register-node") {
            val newNodeIp = call.request.origin.remoteAddress
            val nodeState = this@configureRouting.get<NodeState>()

            if (!nodeState.isRegistered()) return@post call.respond(HttpStatusCode.ExpectationFailed)

            val neighborAddress = nodeState.successorAddress

            if (neighborAddress != null) {
                val response = HttpClient(CIO).post("http://$neighborAddress:8080/replace-predecessor/$newNodeIp}")
                if (response.status == HttpStatusCode.OK) {

                    val registrationState = RegistrationStateDto(successorIpAddress = neighborAddress)

                    nodeState.setSuccessor(address =  newNodeIp)

                    call.respond(registrationState)
                } else {
                    println("Failed to inform neighbor node ($neighborAddress) about new predecessor. Error: ${response.bodyAsText()}")
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } else {
                nodeState.setSuccessor(address = newNodeIp)
                nodeState.setPredecessor(address = newNodeIp)

                val registrationState = RegistrationStateDto()
                call.respond(registrationState)
            }
        }

        post("/replace-predecessor/{nodeIp}") {
            val newNeighborIp = call.request.queryParameters["nodeIp"]!!
            val nodeState = this@configureRouting.get<NodeState>()

            nodeState.setPredecessor(address = newNeighborIp)
            call.respond(HttpStatusCode.OK)
        }

    }
}
