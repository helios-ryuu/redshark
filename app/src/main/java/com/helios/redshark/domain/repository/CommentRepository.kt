package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.CreateCommentInput
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface CommentRepository {
    /** Emits a live list of comments for the given idea, ordered by createdAt ASC. */
    fun getCommentsByIdea(ideaId: UUID): Flow<List<Comment>>

    suspend fun create(input: CreateCommentInput): Comment
}
