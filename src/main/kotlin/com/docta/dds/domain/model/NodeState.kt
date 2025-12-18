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


    fun initializeNodeId() {
        if (nodeId == null) nodeId = Uuid.random()
    }
    fun getNodeId(): String {
        return nodeId?.toString() ?: "null"
    }

    fun isRegistered(): Boolean = nodeId != null


    fun replaceNeighbor(current: String, new: String): Boolean {
        if (successorAddress == current) {
            successorAddress = new
            return true
        } else if (predecessorAddress == current) {
            predecessorAddress = new
            return true
        }
        return false
    }

    fun setSuccessor(address: String) {
        successorAddress = address
    }
    fun setPredecessor(address: String) {
        predecessorAddress = address
    }


    fun registerNode() {
        initializeNodeId()
        isLeader = true
    }

    fun registerNode(registrationState: RegistrationStateDto) {
        initializeNodeId()
        successorAddress = registrationState.successorIpAddress
        predecessorAddress = registrationState.predecessorIpAddress
    }

}