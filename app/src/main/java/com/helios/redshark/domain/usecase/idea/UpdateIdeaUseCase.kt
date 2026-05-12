package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.UpdateIdeaInput
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

class UpdateIdeaUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    suspend operator fun invoke(id: UUID, input: UpdateIdeaInput): Idea {
        if (input.title.length !in 3..120)
            throw AppException.ValidationException("title", "Tiêu đề phải từ 3 đến 120 ký tự.")
        if (input.description != null && input.description.length > 5000)
            throw AppException.ValidationException("description", "Mô tả không vượt quá 5000 ký tự.")
        return ideaRepository.update(id, input)
    }
}
