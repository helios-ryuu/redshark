package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.repository.IssueRepository
import java.util.UUID
import javax.inject.Inject

class GetIssueDetailUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    suspend operator fun invoke(id: UUID): Issue = issueRepository.getIssueDetail(id)
}
