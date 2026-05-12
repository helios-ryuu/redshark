package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

data class Message(
    val id: UUID,
    val conversationId: UUID,
    val senderId: String,
    val content: String,
    val createdAt: Instant,
)

data class SendMessageInput(
    val conversationId: UUID,
    val content: String,
)
