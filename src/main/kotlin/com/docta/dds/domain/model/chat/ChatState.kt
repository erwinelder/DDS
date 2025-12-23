package com.docta.dds.domain.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatState(
    val seq: Long = 0,
    val messageHistory: List<ChatMessage> = emptyList()
)
