package com.docta.dds.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class NodeStateDto(
    val nodeId: String?,
    val isLeader: Boolean,
    val successorAddress: String?,
    val predecessorAddress: String?
)
