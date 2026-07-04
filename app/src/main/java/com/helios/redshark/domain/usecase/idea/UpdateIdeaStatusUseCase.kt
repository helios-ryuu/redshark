package com.helios.redshark.domain.usecase.idea

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

private val IDEA_STATE_MACHINE = mapOf(
    IdeaStatus.ACTIVE to setOf(IdeaStatus.CLOSED, IdeaStatus.CANCELLED),
    IdeaStatus.CLOSED to emptySet(),
    IdeaStatus.CANCELLED to emptySet(),
)

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class UpdateIdeaStatusUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(id: UUID, newStatus: IdeaStatus): Idea {
        val idea = ideaRepository.getIdeaDetail(id)
        val allowed = IDEA_STATE_MACHINE[idea.status] ?: emptySet()
        if (newStatus !in allowed)
            throw AppException.InvalidStateTransitionException(idea.status.name, newStatus.name)
        return ideaRepository.updateStatus(id, newStatus)
    }
}
