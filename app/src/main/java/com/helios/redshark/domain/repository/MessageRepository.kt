package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface MessageRepository {
    /** Emits a live list of conversations for the current user, newest first. */
    fun getConversations(): Flow<List<Conversation>>

    /** Emits a live list of messages in the given conversation, oldest first. */
    fun getMessages(conversationId: UUID): Flow<List<Message>>

    suspend fun sendMessage(input: SendMessageInput): Message

    /** Returns null when no DIRECT conversation exists between the current user and [peerId]. */
    suspend fun findDirectConversation(peerId: String): Conversation?

    suspend fun createDirectConversation(peerId: String): Conversation
}
