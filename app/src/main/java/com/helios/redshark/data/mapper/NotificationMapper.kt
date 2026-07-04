package com.helios.redshark.data.mapper

// File nay chuyen doi giua DTO luu tru va model domain.

import com.helios.redshark.data.remote.firestore.dto.NotificationDto
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import java.time.Instant
import java.util.UUID

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun NotificationDto.toDomain(): Notification = Notification(
    id = UUID.fromString(id),
    recipientId = recipientId,
    actorId = actorId,
    type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.ISSUE_CREATED),
    targetType = runCatching { NotificationTargetType.valueOf(targetType) }.getOrDefault(NotificationTargetType.IDEA),
    targetId = UUID.fromString(targetId),
    message = message,
    isRead = isRead,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
)
