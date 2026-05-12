package com.helios.redshark.domain.usecase.idea
import com.helios.redshark.domain.model.IdeaReaction
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject
class SetIdeaReactionUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
) {
    suspend operator fun invoke(ideaId: UUID, reaction: IdeaReaction) {
        ideaRepository.setReaction(ideaId, reaction)
    }
}
