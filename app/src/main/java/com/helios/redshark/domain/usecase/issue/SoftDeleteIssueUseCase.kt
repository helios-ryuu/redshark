package com.helios.redshark.domain.usecase.issue

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.repository.IssueRepository
import java.util.UUID
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class SoftDeleteIssueUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    // Sets deletedAt=request.time; ownership enforced server-side (authorId = auth.uid).
    suspend operator fun invoke(id: UUID) =
        issueRepository.softDelete(id)
}
