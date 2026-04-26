package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.ConversationDto
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.ConversationType
import java.util.UUID

fun ConversationDto.toDomain(): Conversation = Conversation(
    id = UUID.fromString(id),
    participantIds = participantIds,
    lastMessageAt = lastMessageAt?.toDate()?.toInstant(),
    lastMessagePreview = lastMessagePreview,
    type = runCatching { ConversationType.valueOf(type) }.getOrDefault(ConversationType.DIRECT),
)
