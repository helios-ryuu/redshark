package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface MessageRepository {
    /** Emits a live list of conversations for the current user, newest first. */
    fun getConversations(): Flow<List<Conversation>>

    /** Emits a live list of messages in the given conversation, oldest first. */
    fun getMessages(conversationId: UUID): Flow<List<Message>>

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun sendMessage(input: SendMessageInput): Message

    /** Returns null when no DIRECT conversation exists between the current user and [peerId]. */
    suspend fun findDirectConversation(peerId: String): Conversation?

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun createDirectConversation(peerId: String): Conversation

    /** Marks the conversation as read for the current user. */
    suspend fun markConversationRead(conversationId: UUID)
}
