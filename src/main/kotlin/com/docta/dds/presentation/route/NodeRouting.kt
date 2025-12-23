package com.docta.dds.presentation.route

import com.docta.dds.presentation.controller.NodeRestController
import com.docta.dds.presentation.service.NodeService
import com.docta.drpc.core.network.server.processPostRoute
import io.ktor.server.routing.Routing
import io.ktor.server.routing.route

fun Routing.configureNodeRouting(
    restController: NodeRestController,
    service: NodeService
) {
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

        processPostRoute(restController.killPath) {
            service.kill()
        }

        processPostRoute(restController.replaceSuccessorsPath) {
            service.replaceSuccessors(successors = get(0))
        }

        processPostRoute(restController.replacePredecessorsPath) {
            service.replacePredecessors(predecessors = get(0))
        }


        processPostRoute(restController.proclaimLeaderPath) {
            service.proclaimLeader(leaderId = get(0), leaderAddress = get(1), chatState = get(2))
        }

        processPostRoute(restController.initiateLonelinessProtocolPath) {
            service.initiateLonelinessProtocol()
        }

    }
}