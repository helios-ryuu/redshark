package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    FAILED,
}

data class Message(
    val id: UUID,
    val conversationId: UUID,
    val senderId: String,
    val content: String,
    val createdAt: Instant,
    val status: MessageDeliveryStatus = MessageDeliveryStatus.SENT,
)

