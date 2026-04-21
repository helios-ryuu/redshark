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
            throw AppException.ValidationException("requesterId", "Khong the gui yeu cau cho chinh idea cua ban.")
        }
        if (idea.collaboratorIds.contains(requesterId)) {
            throw AppException.ValidationException("requesterId", "Ban da la cong tac vien cua idea nay.")
        }

        notificationRepository.create(
            CreateNotificationInput(
                recipientId = idea.authorId,
                actorId = requesterId,
                type = NotificationType.COLLAB_REQUEST,
                targetType = NotificationTargetType.IDEA,
                targetId = ideaId,
                message = "Co nguoi dung muon tham gia idea cua ban.",
            )
        )
    }
}

