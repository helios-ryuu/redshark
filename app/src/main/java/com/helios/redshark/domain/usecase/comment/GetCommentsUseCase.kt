package com.helios.redshark.domain.usecase.comment

import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(ideaId: UUID): Flow<List<Comment>> =
        commentRepository.getCommentsByIdea(ideaId)
}
