package com.docta.dds.domain.model.node

import com.docta.dds.domain.model.chat.ChatState
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationState(
    val leaderId: String,
    val leaderAddress: String,
    val successors: List<String>,
    val predecessors: List<String>,
    val chatState: ChatState
)