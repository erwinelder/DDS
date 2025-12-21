package com.docta.dds.domain.model

import com.docta.dds.presentation.model.RegistrationStateDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object NodeState {

    var nodeId: Uuid? = null
        private set
    val nodeAddress: String
        get() = System.getenv("NODE_ADDRESS")

    var leaderId: Uuid? = null
        private set
    var leaderAddress: String? = null
        private set
    val isLeader: Boolean
        get() = nodeAddress == leaderAddress

    var successorAddress: String? = null
        private set
    var predecessorAddress: String? = null
        private set
    var prePredecessorAddress: String? = null
        private set


    fun getLeaderIdOrNull(): String? = leaderId?.toString()

    fun initializeNodeId() {
        if (nodeId == null) nodeId = Uuid.random()
    }
    fun getNodeIdOrNull(): String? = nodeId?.toString()

    fun isRegistered(): Boolean = nodeId != null


    fun proclaimNodeLeader() {
        leaderId = nodeId
        leaderAddress = nodeAddress
    }


    fun setSuccessor(address: String?) {
        successorAddress = address
    }
    fun setPredecessor(address: String?) {
        predecessorAddress = address
    }
    fun setPrePredecessor(address: String?) {
        prePredecessorAddress = address
    }

    fun resetAllNeighbors() {
        successorAddress = null
        predecessorAddress = null
        prePredecessorAddress = null
    }


    fun registerNode() {
        initializeNodeId()
        proclaimNodeLeader()
    }

    fun registerNode(registrationState: RegistrationStateDto) {
        initializeNodeId()
        leaderId = registrationState.leaderId?.let { Uuid.parse(uuidString = it) }
        leaderAddress = registrationState.leaderAddress
        successorAddress = registrationState.successorAddress
        predecessorAddress = registrationState.predecessorAddress
        prePredecessorAddress = registrationState.prePredecessorAddress
    }

}