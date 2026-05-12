package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.MessageDto
import com.helios.redshark.domain.model.Message
import java.time.Instant
import java.util.UUID

fun MessageDto.toDomain(): Message = Message(
    id = UUID.fromString(id),
    conversationId = UUID.fromString(conversationId),
    senderId = senderId,
    content = content,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
)
