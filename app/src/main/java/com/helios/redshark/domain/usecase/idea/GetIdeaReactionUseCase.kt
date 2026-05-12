package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.domain.model.IdeaReaction
import com.helios.redshark.domain.repository.IdeaRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetIdeaReactionUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
) {
    operator fun invoke(ideaId: UUID): Flow<IdeaReaction> =
        ideaRepository.getReaction(ideaId)
}

