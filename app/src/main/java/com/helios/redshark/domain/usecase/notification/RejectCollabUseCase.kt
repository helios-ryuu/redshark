package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

class RejectCollabUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(notification: Notification) {
        if (notification.type != NotificationType.COLLAB_REQUEST) {
            throw AppException.ValidationException("notificationType", "Yeu cau cong tac khong hop le.")
        }
        val requesterId = notification.actorId
            ?: throw AppException.ValidationException("actorId", "Khong tim thay nguoi gui yeu cau.")

        notificationRepository.markAsRead(notification.id)
        notificationRepository.create(
            CreateNotificationInput(
                recipientId = requesterId,
                actorId = notification.recipientId,
                type = NotificationType.COLLAB_REJECTED,
                targetType = NotificationTargetType.IDEA,
                targetId = notification.targetId,
                message = "Yeu cau cong tac cua ban da bi tu choi.",
            )
        )
    }
}

