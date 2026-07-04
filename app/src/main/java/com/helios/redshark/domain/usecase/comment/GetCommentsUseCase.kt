package com.helios.redshark.domain.usecase.comment

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    operator fun invoke(ideaId: UUID): Flow<List<Comment>> =
        commentRepository.getCommentsByIdea(ideaId)
}
