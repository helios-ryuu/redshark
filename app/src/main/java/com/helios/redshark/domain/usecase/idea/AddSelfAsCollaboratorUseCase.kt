package com.helios.redshark.domain.usecase.idea

import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

class AddSelfAsCollaboratorUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
    private val auth: FirebaseAuth,
) {
    suspend operator fun invoke(ideaId: UUID) {
        val currentUserId = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        ideaRepository.addCollaborator(ideaId, currentUserId)
    }
}

