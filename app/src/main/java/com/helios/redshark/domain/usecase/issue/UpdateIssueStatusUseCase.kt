package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.ISSUE_STATE_MACHINE
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssueStatus
import com.helios.redshark.domain.repository.IssueRepository
import java.util.UUID
import javax.inject.Inject

class UpdateIssueStatusUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    /**
     * Validates the requested transition against the state machine before persisting.
     *
     * Allowed transitions (see [ISSUE_STATE_MACHINE]):
     *   OPEN        → IN_PROGRESS | CANCELLED
     *   IN_PROGRESS → CLOSED
     *   CLOSED      → (terminal, no transitions)
     *   CANCELLED   → (terminal, no transitions)
     *
     * @throws [AppException.InvalidStateTransitionException] if the transition is illegal.
     */
    suspend operator fun invoke(id: UUID, newStatus: IssueStatus): Issue {
        val current = issueRepository.getIssueDetail(id)

        val allowedTargets = ISSUE_STATE_MACHINE[current.status] ?: emptySet()
        if (newStatus !in allowedTargets) {
            throw AppException.InvalidStateTransitionException(
                from = current.status.name,
                to = newStatus.name
            )
        }

        return issueRepository.updateStatus(id, newStatus)
    }
}
