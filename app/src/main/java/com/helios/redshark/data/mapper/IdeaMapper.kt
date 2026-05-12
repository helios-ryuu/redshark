package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.IdeaDto
import com.helios.redshark.data.remote.firestore.dto.MediaAttachmentDto
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.MediaAttachment
import com.helios.redshark.domain.model.MediaType
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
    mediaAttachments = mediaAttachments.mapNotNull { it.toDomainOrNull() },
    upvoteCount = upvoteCount.toInt(),
    commentCount = commentCount.toInt(),
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
    updatedAt = updatedAt?.toDate()?.toInstant() ?: Instant.now(),
    deletedAt = deletedAt?.toDate()?.toInstant(),
)

fun MediaAttachmentDto.toDomainOrNull(): MediaAttachment? {
    val attachmentId = runCatching { UUID.fromString(id) }.getOrNull() ?: return null
    val mediaType = runCatching { MediaType.valueOf(type) }.getOrDefault(MediaType.IMAGE)
    return MediaAttachment(
        id = attachmentId,
        url = url,
        type = mediaType,
        mimeType = mimeType,
        fileName = fileName,
        sizeBytes = sizeBytes,
        createdBy = createdBy,
        createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
    )
}

fun MediaAttachment.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id.toString(),
    "url" to url,
    "type" to type.name,
    "mimeType" to mimeType,
    "fileName" to fileName,
    "sizeBytes" to sizeBytes,
    "createdBy" to createdBy,
    "createdAt" to com.google.firebase.Timestamp(java.util.Date.from(createdAt)),
)
