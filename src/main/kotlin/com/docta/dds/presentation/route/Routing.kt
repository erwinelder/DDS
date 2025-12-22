package com.docta.dds.presentation.route

import com.docta.dds.presentation.controller.NodeRestController
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.server.processPostRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    restController: NodeRestController,
    service: NodeService
) {
    routing {
        route(restController.serviceRoute) {

            processPostRoute(restController.getStatePath) {
                service.getState()
            }

            processPostRoute(restController.isAlivePath) {
                service.isAlive()
            }


            processPostRoute(restController.joinPath) {
                service.join(greeterIpAddress = get(0))
            }

            processPostRoute(restController.registerNodePath) {
                service.registerNode()
            }

            processPostRoute(restController.leavePath) {
                service.leave()
            }

            processPostRoute(restController.replaceSuccessorsPath) {
                service.replaceSuccessors(successors = get(0))
            }

            processPostRoute(restController.replacePredecessorsPath) {
                service.replacePredecessors(predecessors = get(0))
            }


            processPostRoute(restController.proclaimLeaderPath) {
                service.proclaimLeader(leaderId = get(0), leaderAddress = get(1))
            }

            processPostRoute(restController.initiateLonelinessProtocolPath) {
                service.initiateLonelinessProtocol()
            }

        }
    }
}
