package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

enum class ConversationType { DIRECT }

data class Conversation(
    val id: UUID,
    val participantIds: List<String>,
    val lastMessageAt: Instant?,
    val lastMessagePreview: String?,
    val lastMessageSenderId: String?,
    val hasUnread: Boolean,
    val type: ConversationType,
)
