package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.repository.IssueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetHomeFeedUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    /**
     * TC-C17: Home tab shows OPEN issues from OTHER users only.
     * The repository query already filters by status=OPEN and excludes the current user;
     * we additionally strip any soft-deleted items on the client for safety.
     */
    operator fun invoke(): Flow<List<Issue>> =
        issueRepository.getOpenIssuesFromOthers()
            .map { issues -> issues.filter { it.deletedAt == null } }
}
