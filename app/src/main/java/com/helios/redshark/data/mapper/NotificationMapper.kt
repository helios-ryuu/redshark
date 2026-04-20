package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.NotificationDto
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import java.time.Instant
import java.util.UUID

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
