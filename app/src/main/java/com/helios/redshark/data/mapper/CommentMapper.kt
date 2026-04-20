package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.CommentDto
import com.helios.redshark.domain.model.Comment
import java.time.Instant
import java.util.UUID

fun CommentDto.toDomain(): Comment = Comment(
    id = UUID.fromString(id),
    ideaId = UUID.fromString(ideaId),
    authorId = authorId,
    content = content,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
)
