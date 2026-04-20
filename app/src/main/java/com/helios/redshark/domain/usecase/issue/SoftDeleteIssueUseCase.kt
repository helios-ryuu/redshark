package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.domain.repository.IssueRepository
import java.util.UUID
import javax.inject.Inject

class SoftDeleteIssueUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    // Sets deletedAt=request.time; ownership enforced server-side (authorId = auth.uid).
    suspend operator fun invoke(id: UUID) =
        issueRepository.softDelete(id)
}
