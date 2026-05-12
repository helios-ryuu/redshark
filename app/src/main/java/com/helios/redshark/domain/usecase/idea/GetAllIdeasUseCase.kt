package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.repository.IdeaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllIdeasUseCase @Inject constructor(private val repo: IdeaRepository) {
    operator fun invoke(): Flow<List<Idea>> = repo.getAllIdeas()
}
