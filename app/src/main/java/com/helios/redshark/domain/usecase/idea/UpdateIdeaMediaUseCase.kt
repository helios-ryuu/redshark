package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.MediaAttachment
import com.helios.redshark.domain.repository.IdeaRepository
import java.util.UUID
import javax.inject.Inject

class UpdateIdeaMediaUseCase @Inject constructor(
    private val ideaRepository: IdeaRepository,
) {
    suspend operator fun invoke(ideaId: UUID, mediaAttachments: List<MediaAttachment>): Idea =
        ideaRepository.updateMediaAttachments(ideaId, mediaAttachments)
}
