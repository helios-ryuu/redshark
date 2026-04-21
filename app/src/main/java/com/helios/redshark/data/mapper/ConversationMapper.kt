package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.ConversationDto
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.ConversationType
import java.time.Instant
import java.util.UUID

fun ConversationDto.toDomain(): Conversation = Conversation(
    id = UUID.fromString(id),
    type = runCatching { ConversationType.valueOf(type) }.getOrDefault(ConversationType.DIRECT),
    participantIds = participantIds,
    directKey = directKey,
    lastMessage = lastMessage,
    lastMessageAt = lastMessageAt?.toDate()?.toInstant(),
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
)

