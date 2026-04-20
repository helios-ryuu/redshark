package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

class SoftDeleteIdeaUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository
) {
    // Sets deletedAt=request.time on the server; physical row is preserved.
    // Client must filter deletedAt IS NULL after this call.
    suspend operator fun invoke(id: UUID) =
        ideaRepository.softDelete(id)
}
