package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.Instant
import java.util.UUID

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class NotificationType {
    ISSUE_CREATED,
    COLLAB_REQUEST,
    COLLAB_ACCEPTED,
    COLLAB_REJECTED,
    COMMENT
}

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class NotificationTargetType { IDEA, ISSUE, COMMENT }

// Model du lieu nay giu cac truong can truyen giua cac lop.
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

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class CreateNotificationInput(
    val recipientId: String,
    val actorId: String?,
    val type: NotificationType,
    val targetType: NotificationTargetType,
    val targetId: UUID,
    val message: String
)
