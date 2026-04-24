package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.Message
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface MessageRepository {
    fun getMyConversations(): Flow<List<Conversation>>

    fun getMessages(conversationId: UUID): Flow<List<Message>>

    suspend fun findOrCreateDirectConversation(peerId: String): Conversation

    suspend fun sendMessage(conversationId: UUID, content: String): Message
}

