package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

data class Comment(
    val id: UUID,
    val ideaId: UUID,
    val authorId: String,
    val content: String,     // 1–1000 chars
    val createdAt: Instant
)

data class CreateCommentInput(
    val ideaId: UUID,
    val content: String
)
