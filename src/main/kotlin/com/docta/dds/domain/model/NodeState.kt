package com.docta.dds.domain.model

import com.docta.dds.presentation.model.RegistrationStateDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object NodeState {

    var nodeUuid: Uuid? = null
        private set
    val nodeAddress: String
        get() = System.getenv("NODE_ADDRESS")

    var leaderUuid: Uuid? = null
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


    fun initializeNodeId() {
        if (nodeUuid == null) nodeUuid = Uuid.random()
    }
    fun getNodeId(): Uuid = nodeUuid
        ?: throw IllegalStateException("Node ID is null for node $nodeAddress")
    fun getNodeIdString(): String = nodeUuid?.toString()
        ?: throw IllegalStateException("Node ID is null for node $nodeAddress")
    fun getNodeIdStringOrNull(): String? = nodeUuid?.toString()

    fun getLeaderId(): Uuid = leaderUuid
        ?: throw IllegalStateException("Leader ID is null for node $nodeAddress")
    fun getLeaderIdString(): String = leaderUuid?.toString()
        ?: throw IllegalStateException("Leader ID is null for node $nodeAddress")
    fun getLeaderIdStringOrNull(): String? = leaderUuid?.toString()

    fun getLeaderAddressString(): String = leaderAddress
        ?: throw IllegalStateException("Leader address is null for node $nodeAddress")

    fun isRegistered(): Boolean = nodeUuid != null


    fun proclaimAsLeader() {
        leaderUuid = nodeUuid
        leaderAddress = nodeAddress
    }

    fun updateLeader(leaderId: String, leaderAddress: String) {
        this.leaderUuid = Uuid.parse(uuidString = leaderId)
        this.leaderAddress = leaderAddress
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
        proclaimAsLeader()
    }

    fun registerNode(registrationState: RegistrationStateDto) {
        initializeNodeId()
        leaderUuid = Uuid.parse(uuidString = registrationState.leaderId)
        leaderAddress = registrationState.leaderAddress
        successorAddress = registrationState.successorAddress
        predecessorAddress = registrationState.predecessorAddress
        prePredecessorAddress = registrationState.prePredecessorAddress
    }

}