package com.helios.redshark.domain.usecase.idea

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.UpdateIdeaInput
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class UpdateIdeaUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(id: UUID, input: UpdateIdeaInput): Idea {
        if (input.title.length !in 3..120)
            throw AppException.ValidationException("title", "Tiêu đề phải từ 3 đến 120 ký tự.")
        if (input.description != null && input.description.length > 5000)
            throw AppException.ValidationException("description", "Mô tả không vượt quá 5000 ký tự.")
        return ideaRepository.update(id, input)
    }
}
