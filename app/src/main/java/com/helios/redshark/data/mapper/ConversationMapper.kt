package com.helios.redshark.data.mapper

// File nay chuyen doi giua DTO luu tru va model domain.

import com.helios.redshark.data.remote.firestore.dto.ConversationDto
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.ConversationType
import java.util.UUID

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun ConversationDto.toDomain(): Conversation = Conversation(
    id = UUID.fromString(id),
    participantIds = participantIds,
    lastMessageAt = lastMessageAt?.toDate()?.toInstant(),
    lastMessagePreview = lastMessagePreview,
    lastMessageSenderId = lastMessageSenderId,
    hasUnread = hasUnread,
    type = runCatching { ConversationType.valueOf(type) }.getOrDefault(ConversationType.DIRECT),
)
