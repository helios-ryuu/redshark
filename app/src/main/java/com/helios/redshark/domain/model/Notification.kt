package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

enum class NotificationType {
    ISSUE_CREATED,
    COLLAB_REQUEST,
    COLLAB_ACCEPTED,
    COLLAB_REJECTED,
    COMMENT
}

enum class NotificationTargetType { IDEA, ISSUE, COMMENT }

data class Notification(
    val id: UUID,
    val recipientId: String,
    val actorId: String?,                // null for system-generated notifications
    val type: NotificationType,
    val targetType: NotificationTargetType,
    val targetId: UUID,
    val message: String,
    val isRead: Boolean,
    val createdAt: Instant
)

data class CreateNotificationInput(
    val recipientId: String,
    val actorId: String?,
    val type: NotificationType,
    val targetType: NotificationTargetType,
    val targetId: UUID,
    val message: String
)