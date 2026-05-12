package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

class GetIdeaDetailUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    /**
     * TC-C23: soft-deleted ideas are physically present in the DB but must appear
     * as "not found" to the client (deep link / back-navigation guard).
     */
    suspend operator fun invoke(id: UUID): Idea {
        val idea = ideaRepository.getIdeaDetail(id)
        if (idea.deletedAt != null)
            throw AppException.NotFoundException("Idea")
        return idea
    }
}
