package com.docta.dds.domain.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageRequest(
    val text: String,
    val senderAddress: String
)