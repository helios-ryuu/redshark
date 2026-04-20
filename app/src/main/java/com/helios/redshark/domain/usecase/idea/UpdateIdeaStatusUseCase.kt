package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.core.AppException
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

class UpdateIdeaStatusUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    suspend operator fun invoke(id: UUID, newStatus: IdeaStatus): Idea {
        val idea = ideaRepository.getIdeaDetail(id)
        val allowed = IDEA_STATE_MACHINE[idea.status] ?: emptySet()
        if (newStatus !in allowed)
            throw AppException.InvalidStateTransitionException(idea.status.name, newStatus.name)
        return ideaRepository.updateStatus(id, newStatus)
    }
}
