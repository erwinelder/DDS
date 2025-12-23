package com.docta.dds.domain.model.node

import kotlinx.serialization.Serializable

@Serializable
data class NodeState(
    val nodeId: String?,
    val nodeAddress: String,
    val leaderId: String?,
    val leaderAddress: String?,
    val isLeader: Boolean,
    val successorAddress: String?,
    val grandSuccessorAddress: String?,
    val predecessorAddress: String?
)