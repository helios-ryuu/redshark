package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.MessageRepository
import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

class AcceptCollabUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
    private val notificationRepository: NotificationRepository,
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(notification: Notification): Conversation {
        if (notification.type != NotificationType.COLLAB_REQUEST) {
            throw AppException.ValidationException("notificationType", "Invalid collaboration request.")
        }
        val requesterId = notification.actorId
            ?: throw AppException.ValidationException("actorId", "Requester was not found.")

        ideaRepository.addCollaborator(notification.targetId, requesterId)
        val conversation = messageRepository.findOrCreateDirectConversation(requesterId)
        notificationRepository.markAsRead(notification.id)
        notificationRepository.create(
            CreateNotificationInput(
                recipientId = requesterId,
                actorId = notification.recipientId,
                type = NotificationType.COLLAB_ACCEPTED,
                targetType = NotificationTargetType.IDEA,
                targetId = notification.targetId,
                message = "Your collaboration request has been accepted.",
            )
        )
        return conversation
    }
}

