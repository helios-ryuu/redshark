package com.helios.redshark.domain.usecase.notification

import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

class RejectCollabUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth,
) {
    suspend operator fun invoke(notification: Notification) {
        val currentUserId = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        val actorId = notification.actorId
            ?: throw AppException.ValidationException("Thiếu thông tin người gửi yêu cầu.")
        notificationRepository.markAsRead(notification.id)
        notificationRepository.create(
            CreateNotificationInput(
                recipientId = actorId,
                actorId = currentUserId,
                type = NotificationType.COLLAB_REJECTED,
                targetType = NotificationTargetType.IDEA,
                targetId = notification.targetId,
                message = "Yêu cầu cộng tác của bạn đã bị từ chối.",
            )
        )
    }
}
