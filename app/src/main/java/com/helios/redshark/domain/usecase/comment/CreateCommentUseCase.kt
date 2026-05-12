package com.helios.redshark.domain.usecase.comment

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.CreateCommentInput
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.CommentRepository
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val ideaRepository: IdeaRepository,
    private val notificationRepository: NotificationRepository
) {
    /**
     * Workflow:
     * 1. Validate content length: 1–1000 chars (blank counts as invalid).
     * 2. Persist comment.
     * 3. Notify idea author with type=COMMENT (skipped if author comments on their own idea).
     *
     * @param currentUserId Firebase UID of the authenticated commenter.
     */
    suspend operator fun invoke(input: CreateCommentInput, currentUserId: String): Comment {
        // Trim to reject whitespace-only submissions while still counting real content
        if (input.content.isBlank() || input.content.length > 1000)
            throw AppException.ValidationException(
                "content", "Bình luận phải từ 1 đến 1000 ký tự."
            )

        val comment = commentRepository.create(input)

        val idea = ideaRepository.getIdeaDetail(input.ideaId)
        if (idea.authorId != currentUserId) {
            notificationRepository.create(
                CreateNotificationInput(
                    recipientId = idea.authorId,
                    actorId = currentUserId,
                    type = NotificationType.COMMENT,
                    targetType = NotificationTargetType.COMMENT,
                    targetId = comment.id,
                    message = "Có bình luận mới trên ý tưởng của bạn."
                )
            )
        }
        return comment
    }
}
