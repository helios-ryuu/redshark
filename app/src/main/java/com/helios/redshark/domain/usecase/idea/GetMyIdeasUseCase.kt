package com.helios.redshark.domain.usecase.idea

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.repository.IdeaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class GetMyIdeasUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    // Per spec: filter deletedAt IS NULL on the client side
    operator fun invoke(): Flow<List<Idea>> =
        ideaRepository.getMyIdeas()
            .map { ideas -> ideas.filter { it.deletedAt == null } }
}
