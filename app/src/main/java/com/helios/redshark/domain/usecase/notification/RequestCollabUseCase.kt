package com.helios.redshark.domain.usecase.notification

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class RequestCollabUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(ideaId: UUID) {
        val currentUserId = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        val idea = ideaRepository.getIdeaDetail(ideaId)
        if (idea.status != IdeaStatus.ACTIVE) throw AppException.IdeaNotActiveException()
        if (idea.authorId == currentUserId)
            throw AppException.ValidationException("Không thể gửi yêu cầu tham gia idea của chính mình.")
        if (currentUserId in idea.collaboratorIds)
            throw AppException.ValidationException("Bạn đã là cộng tác viên của idea này.")
        notificationRepository.create(
            CreateNotificationInput(
                recipientId = idea.authorId,
                actorId = currentUserId,
                type = NotificationType.COLLAB_REQUEST,
                targetType = NotificationTargetType.IDEA,
                targetId = ideaId,
                message = "Một người dùng muốn tham gia cộng tác với idea của bạn.",
            )
        )
    }
}
