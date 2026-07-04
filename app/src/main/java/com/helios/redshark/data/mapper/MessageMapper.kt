package com.helios.redshark.data.mapper

// File nay chuyen doi giua DTO luu tru va model domain.

import com.helios.redshark.data.remote.firestore.dto.MessageDto
import com.helios.redshark.domain.model.Message
import java.time.Instant
import java.util.UUID

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun MessageDto.toDomain(): Message = Message(
    id = UUID.fromString(id),
    conversationId = UUID.fromString(conversationId),
    senderId = senderId,
    content = content,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
)
