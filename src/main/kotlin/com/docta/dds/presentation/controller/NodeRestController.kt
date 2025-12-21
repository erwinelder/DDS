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

    val isAlivePath: String
        get() = "/isAlive"


    val joinPath: String
        get() = "/join"

    val registerNodePath: String
        get() = "/registerNode"

    val leavePath: String
        get() = "/leave"

    val replaceSuccessorPath: String
        get() = "/replaceSuccessor"

    val replacePredecessorPath: String
        get() = "/replacePredecessor"


    val proclaimLeaderPath: String
        get() = "/proclaimLeader"

    val initiateLonelinessProtocolPath: String
        get() = "/initiateLonelinessProtocol"

}