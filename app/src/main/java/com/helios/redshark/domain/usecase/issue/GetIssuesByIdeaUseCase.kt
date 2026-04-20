package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.repository.IssueRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetIssuesByIdeaUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    operator fun invoke(ideaId: UUID): Flow<List<Issue>> =
        issueRepository.getIssuesByIdea(ideaId)
}
