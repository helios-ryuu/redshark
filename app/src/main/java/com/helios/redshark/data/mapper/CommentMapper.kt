package com.helios.redshark.data.mapper

// File nay chuyen doi giua DTO luu tru va model domain.

import com.helios.redshark.data.remote.firestore.dto.CommentDto
import com.helios.redshark.domain.model.Comment
import java.time.Instant
import java.util.UUID

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
fun CommentDto.toDomain(): Comment = Comment(
    id = UUID.fromString(id),
    ideaId = UUID.fromString(ideaId),
    authorId = authorId,
    content = content,
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
)
