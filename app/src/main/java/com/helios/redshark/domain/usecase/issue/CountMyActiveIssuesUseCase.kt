package com.helios.redshark.domain.usecase.issue

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.repository.IssueRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class CountMyActiveIssuesUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    // Active = status IN {OPEN, IN_PROGRESS} AND deletedAt IS NULL
    suspend operator fun invoke(): Int =
        issueRepository.countMyActiveIssues()
}
