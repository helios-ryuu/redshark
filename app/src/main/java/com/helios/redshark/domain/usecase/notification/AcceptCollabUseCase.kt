package com.helios.redshark.domain.usecase.notification

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.NotificationRepository
import com.helios.redshark.domain.usecase.message.FindOrCreateDirectConversationUseCase
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class AcceptCollabUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth,
    private val findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(notification: Notification) {
        val currentUserId = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        val actorId = notification.actorId
            ?: throw AppException.ValidationException("Thiếu thông tin người gửi yêu cầu.")
        ideaRepository.addCollaborator(notification.targetId, actorId)
        findOrCreateDirectConversationUseCase(actorId)
        notificationRepository.markAsRead(notification.id)
        notificationRepository.create(
            CreateNotificationInput(
                recipientId = actorId,
                actorId = currentUserId,
                type = NotificationType.COLLAB_ACCEPTED,
                targetType = NotificationTargetType.IDEA,
                targetId = notification.targetId,
                message = "Yêu cầu cộng tác của bạn đã được chấp nhận.",
            )
        )
    }
}
