package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.domain.repository.IssueRepository
import javax.inject.Inject

class CountMyActiveIssuesUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    // Active = status IN {OPEN, IN_PROGRESS} AND deletedAt IS NULL
    suspend operator fun invoke(): Int =
        issueRepository.countMyActiveIssues()
}
