package com.docta.dds.domain.model.chat

object ChatContext {

    var seq: Long = 0
        private set

    fun getNextSeq(): Long = seq + 1


    private val messageHistory = mutableListOf<ChatMessage>()
    private val messageBuffer = mutableListOf<ChatMessage>()

    fun processNewMessage(message: ChatMessage) {
        messageBuffer.add(message)
        messageBuffer.sortBy { it.messageId }
        processBuffer()
    }

    private fun processBuffer() {
        val iterator = messageBuffer.iterator()

        while (iterator.hasNext()) {
            val message = iterator.next()

            if (message.messageId == seq + 1) {
                messageHistory.add(message)
                seq++
                iterator.remove()
            } else if (message.messageId > seq + 1) {
                break
            } else {
                iterator.remove()
            }
        }
    }


    fun getChatState(): ChatState {
        return ChatState(
            seq = seq,
            messageHistory = messageHistory.toList()
        )
    }

    fun updateChatState(state: ChatState) {
        seq = state.seq

        messageHistory.clear()
        messageHistory.addAll(state.messageHistory)
        messageBuffer.clear()
    }

}