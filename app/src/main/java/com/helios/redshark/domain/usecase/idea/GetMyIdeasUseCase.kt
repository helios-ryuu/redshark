package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.repository.IdeaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMyIdeasUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    // Per spec: filter deletedAt IS NULL on the client side
    operator fun invoke(): Flow<List<Idea>> =
        ideaRepository.getMyIdeas()
            .map { ideas -> ideas.filter { it.deletedAt == null } }
}
