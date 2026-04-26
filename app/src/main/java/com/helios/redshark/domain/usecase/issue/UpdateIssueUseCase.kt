package com.helios.redshark.domain.usecase.issue

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.UpdateIssueInput
import com.helios.redshark.domain.repository.IssueRepository
import java.util.UUID
import javax.inject.Inject

class UpdateIssueUseCase @Inject constructor(
    private val issueRepository: IssueRepository
) {
    /** TC-C11: validates editable fields then persists. Ownership is enforced server-side. */
    suspend operator fun invoke(id: UUID, input: UpdateIssueInput): Issue {
        if (input.title.length !in 3..120)
            throw AppException.ValidationException("title", "Tiêu đề phải từ 3 đến 120 ký tự.")
        if (input.description != null && input.description.length > 5000)
            throw AppException.ValidationException("description", "Mô tả không vượt quá 5000 ký tự.")
        return issueRepository.update(id, input)
    }
}
