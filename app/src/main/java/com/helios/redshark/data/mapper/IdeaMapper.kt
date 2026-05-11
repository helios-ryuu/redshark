package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.IdeaDto
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import java.time.Instant
import java.util.UUID

fun IdeaDto.toDomain(): Idea = Idea(
    id = UUID.fromString(id),
    authorId = authorId,
    title = title,
    description = description,
    status = runCatching { IdeaStatus.valueOf(status) }.getOrDefault(IdeaStatus.ACTIVE),
    tagIds = tagIds.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() },
    collaboratorIds = collaboratorIds,
    upvoteCount = upvoteCount.toInt(),
    commentCount = commentCount.toInt(),
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
    updatedAt = updatedAt?.toDate()?.toInstant() ?: Instant.now(),
    deletedAt = deletedAt?.toDate()?.toInstant(),
)
