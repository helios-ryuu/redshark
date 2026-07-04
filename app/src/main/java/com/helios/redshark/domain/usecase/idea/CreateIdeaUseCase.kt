package com.helios.redshark.domain.usecase.idea

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.CreateIdeaInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.repository.IdeaRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    private fun validate(title: String, description: String?) {
        if (title.length !in 3..120)
            throw AppException.ValidationException("title", "Tiêu đề phải từ 3 đến 120 ký tự.")
        if (description != null && description.length > 5000)
            throw AppException.ValidationException("description", "Mô tả không vượt quá 5000 ký tự.")
    }
}
