package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.CreateCommentInput
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface CommentRepository {
    /** Emits a live list of comments for the given idea, ordered by createdAt ASC. */
    fun getCommentsByIdea(ideaId: UUID): Flow<List<Comment>>

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun create(input: CreateCommentInput): Comment
}
