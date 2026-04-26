package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateIssueInput
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.IssueRepository
import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

private const val ACTIVE_ISSUE_LIMIT = 20

class CreateIssueUseCase @Inject constructor(
    private val issueRepository: IssueRepository,
    private val ideaRepository: IdeaRepository,
    private val notificationRepository: NotificationRepository
) {
    /**
     * Workflow:
     * 1. Validate title/description lengths.
     * 2. Fetch parent idea — guard: must be ACTIVE (TC-C08), must not be soft-deleted.
     * 3. Count active issues — throw [AppException.IssueLimitExceededException] if ≥ 20 (TC-C10).
     * 4. Create the issue (status=OPEN set by server).
     * 5. Notify the idea author with type=ISSUE_CREATED (skipped for self-issue).
     *
     * @param currentUserId Firebase UID of the authenticated user performing the action.
     */
    suspend operator fun invoke(input: CreateIssueInput, currentUserId: String): Issue {
        validateInput(input)

        // TC-C08: fetch idea BEFORE creating issue so we can guard on its status.
        // Also reuses this object for the notification step — avoids a second network call.
        val idea = ideaRepository.getIdeaDetail(input.ideaId)
        if (idea.deletedAt != null)
            throw AppException.NotFoundException("Idea")
        if (idea.status != IdeaStatus.ACTIVE)
            throw AppException.IdeaNotActiveException()

        enforceActiveIssueLimit()

        val issue = issueRepository.create(input)

        if (idea.authorId != currentUserId) {
            notificationRepository.create(
                CreateNotificationInput(
                    recipientId = idea.authorId,
                    actorId = currentUserId,
                    type = NotificationType.ISSUE_CREATED,
                    targetType = NotificationTargetType.ISSUE,
                    targetId = issue.id,
                    message = "Có issue mới được tạo trong ý tưởng của bạn."
                )
            )
        }
        return issue
    }

    private suspend fun enforceActiveIssueLimit() {
        val count = issueRepository.countMyActiveIssues()
        if (count >= ACTIVE_ISSUE_LIMIT)
            throw AppException.IssueLimitExceededException(ACTIVE_ISSUE_LIMIT)
    }

    private fun validateInput(input: CreateIssueInput) {
        if (input.title.length !in 3..120)
            throw AppException.ValidationException("title", "Tiêu đề phải từ 3 đến 120 ký tự.")
        if (input.description != null && input.description.length > 5000)
            throw AppException.ValidationException("description", "Mô tả không vượt quá 5000 ký tự.")
    }
}
