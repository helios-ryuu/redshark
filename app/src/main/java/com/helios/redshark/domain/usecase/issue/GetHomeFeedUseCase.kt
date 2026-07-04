package com.helios.redshark.domain.usecase.issue

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.repository.IssueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
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
