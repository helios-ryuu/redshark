package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

enum class ConversationType {
    DIRECT,
}

data class Conversation(
    val id: UUID,
    val type: ConversationType,
    val participantIds: List<String>,
    val directKey: String,
    val lastMessage: String?,
    val lastMessageAt: Instant?,
    val createdAt: Instant,
)

