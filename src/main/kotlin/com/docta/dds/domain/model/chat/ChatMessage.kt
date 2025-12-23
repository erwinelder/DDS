package com.docta.dds.domain.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val messageId: Long,
    val message: String,
    val senderAddress: String
)