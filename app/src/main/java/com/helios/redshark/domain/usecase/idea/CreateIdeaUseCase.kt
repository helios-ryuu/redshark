package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.CreateIdeaInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.repository.IdeaRepository
import javax.inject.Inject

class CreateIdeaUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    /**
     * Validates input then delegates to the repository.
     * Title: 3–120 chars. Description: ≤ 5000 chars (may be null).
     */
    suspend operator fun invoke(input: CreateIdeaInput): Idea {
        validate(input.title, input.description)
        return ideaRepository.create(input)
    }

    private fun validate(title: String, description: String?) {
        if (title.length !in 3..120)
            throw AppException.ValidationException("title", "Tiêu đề phải từ 3 đến 120 ký tự.")
        if (description != null && description.length > 5000)
            throw AppException.ValidationException("description", "Mô tả không vượt quá 5000 ký tự.")
    }
}
