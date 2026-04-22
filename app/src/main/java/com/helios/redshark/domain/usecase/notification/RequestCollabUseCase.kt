package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject

class RequestCollabUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(ideaId: UUID, requesterId: String) {
        val idea = ideaRepository.getIdeaDetail(ideaId)

        if (idea.authorId == requesterId) {
            throw AppException.ValidationException("requesterId", "You cannot request collaboration on your own idea.")
        }
        if (idea.collaboratorIds.contains(requesterId)) {
            throw AppException.ValidationException("requesterId", "You are already a collaborator on this idea.")
        }

        notificationRepository.create(
            CreateNotificationInput(
                recipientId = idea.authorId,
                actorId = requesterId,
                type = NotificationType.COLLAB_REQUEST,
                targetType = NotificationTargetType.IDEA,
                targetId = ideaId,
                message = "A user requested to collaborate on your idea.",
            )
        )
    }
}

