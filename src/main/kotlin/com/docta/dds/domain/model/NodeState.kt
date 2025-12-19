package com.docta.dds.domain.model

import com.docta.dds.presentation.model.RegistrationStateDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object NodeState {

    var nodeId: Uuid? = null
        private set
    var isLeader: Boolean = false
        private set

    var successorAddress: String? = null
        private set
    var predecessorAddress: String? = null
        private set
    var prePredecessorAddress: String? = null
        private set


    fun initializeNodeId() {
        if (nodeId == null) nodeId = Uuid.random()
    }
    fun getNodeId(): String = nodeId?.toString() ?: ""
    fun getNodeIdOrNull(): String? = nodeId?.toString()

    fun isRegistered(): Boolean = nodeId != null


    fun setIsLeader(isLeader: Boolean) {
        this.isLeader = isLeader
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
        isLeader = true
    }

    fun registerNode(registrationState: RegistrationStateDto) {
        initializeNodeId()
        successorAddress = registrationState.successorIpAddress
        predecessorAddress = registrationState.predecessorIpAddress
        prePredecessorAddress = registrationState.predecessorOfPredecessorIpAddress
    }

}