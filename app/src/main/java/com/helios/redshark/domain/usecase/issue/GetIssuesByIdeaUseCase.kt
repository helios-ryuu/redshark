package com.helios.redshark.domain.usecase.issue

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.repository.IssueRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class GetIssuesByIdeaUseCase @Inject constructor(
    private val issueRepository: IssueRepository
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    operator fun invoke(ideaId: UUID): Flow<List<Issue>> =
        issueRepository.getIssuesByIdea(ideaId)
}
