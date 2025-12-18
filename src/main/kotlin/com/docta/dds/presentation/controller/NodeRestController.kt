package com.docta.dds.presentation.controller

import com.docta.dds.presentation.service.NodeService

interface NodeRestController : NodeService {

    val hostname: String

    val serviceRoute: String
        get() = "/Node"

    val absoluteUrl: String
        get() = "http://$hostname:8080$serviceRoute"


    val getStatePath: String
        get() = "/getState"

    val joinPath: String
        get() = "/join"

    val registerNodePath: String
        get() = "/registerNode"

    val replacePredecessorPath: String
        get() = "/replacePredecessor"

}