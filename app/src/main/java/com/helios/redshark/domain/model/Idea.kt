package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

enum class IdeaStatus { ACTIVE, CLOSED, CANCELLED }

data class Idea(
    val id: UUID,
    val authorId: String,
    val title: String,                   // 3–120 chars
    val description: String?,            // ≤ 5000 chars; nullable
    val status: IdeaStatus,
    val tagIds: List<UUID>,
    val collaboratorIds: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?              // null = not soft-deleted
)

data class CreateIdeaInput(
    val title: String,
    val description: String?,
    val tagIds: List<UUID> = emptyList()
)

data class UpdateIdeaInput(
    val title: String,
    val description: String?,
    val tagIds: List<UUID> = emptyList()
)
