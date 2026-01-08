package com.docta.dds.domain.model.node

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object NodeContext {

    var nodeUuid: Uuid? = null
        private set
    val nodeAddress: String
        get() = System.getenv("NODE_ADDRESS")

    fun initializeNodeId() {
        if (nodeUuid == null) nodeUuid = Uuid.random()
//        if (nodeUuid == null) nodeUuid = Uuid.parse(
//            uuidString = "00000000-0000-0000-0000-0000000000" +
//                    "0".takeIf { nodeAddress.substringAfterLast('.').length == 1 }.orEmpty() +
//                    nodeAddress.substringAfterLast('.')
//        )
    }
    fun getNodeId(): Uuid = nodeUuid
        ?: throw IllegalStateException("Node ID is null for node $nodeAddress")
    fun getNodeIdString(): String = nodeUuid?.toString()
        ?: throw IllegalStateException("Node ID is null for node $nodeAddress")
    fun getNodeIdStringOrNull(): String? = nodeUuid?.toString()

    fun isRegistered(): Boolean = nodeUuid != null


    var leaderUuid: Uuid? = null
        private set
    var leaderAddress: String? = null
        private set
    val isLeader: Boolean
        get() = nodeAddress == leaderAddress

    fun getLeaderId(): Uuid = leaderUuid
        ?: throw IllegalStateException("Leader ID is null for node $nodeAddress")
    fun getLeaderIdString(): String = leaderUuid?.toString()
        ?: throw IllegalStateException("Leader ID is null for node $nodeAddress")
    fun getLeaderIdStringOrNull(): String? = leaderUuid?.toString()

    fun getLeaderAddressString(): String = leaderAddress
        ?: throw IllegalStateException("Leader address is null for node $nodeAddress")

    fun proclaimAsLeader() {
        leaderUuid = nodeUuid
        leaderAddress = nodeAddress
    }

    fun updateLeader(leaderId: String, leaderAddress: String) {
        this.leaderUuid = Uuid.parse(uuidString = leaderId)
        this.leaderAddress = leaderAddress
    }


    private val successors = mutableListOf<String>()
    val successorAddress: String?
        get() = successors.getOrNull(0)
    val grandSuccessorAddress: String?
        get() = successors.getOrNull(1)

    private val predecessors = mutableListOf<String>()
    val predecessorAddress: String?
        get() = predecessors.getOrNull(0)

    fun getSuccessors(): List<String> = successors.toList()
    fun getPredecessors(): List<String> = predecessors.toList()

    fun setSuccessors(addresses: List<String>) {
        successors.clear()
        successors.addAll(addresses)
    }
    fun setPredecessors(addresses: List<String>) {
        predecessors.clear()
        predecessors.addAll(addresses)
    }

    fun removeGrandSuccessor() {
        if (successors.size >= 2) {
            successors.removeAt(1)
        }
    }
    fun removeAllNeighbors() {
        successors.clear()
        predecessors.clear()
    }

    fun successorsEqual(other: List<String>): Boolean {
        val nodeAddress = nodeAddress
        val other = other.filter { it != nodeAddress }

        if (successors.size != other.size) return false
        for (i in successors.indices) {
            if (successors[i] != other[i]) return false
        }
        return true
    }
    fun predecessorsEqual(other: List<String>): Boolean {
        val nodeAddress = nodeAddress
        val other = other.filter { it != nodeAddress }

        if (predecessors.size != other.size) return false
        for (i in predecessors.indices) {
            if (predecessors[i] != other[i]) return false
        }
        return true
    }


    fun registerNode() {
        initializeNodeId()
        proclaimAsLeader()
    }

    fun registerNode(registrationState: RegistrationState) {
        initializeNodeId()
        updateLeader(leaderId = registrationState.leaderId, leaderAddress = registrationState.leaderAddress)
        setSuccessors(addresses = registrationState.successors)
        setPredecessors(addresses = registrationState.predecessors)
    }

}