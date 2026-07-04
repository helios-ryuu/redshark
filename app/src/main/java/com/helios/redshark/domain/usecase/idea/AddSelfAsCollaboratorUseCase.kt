package com.helios.redshark.domain.usecase.idea

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class AddSelfAsCollaboratorUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
    private val auth: FirebaseAuth,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(ideaId: UUID) {
        val currentUserId = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        ideaRepository.addCollaborator(ideaId, currentUserId)
    }
}

