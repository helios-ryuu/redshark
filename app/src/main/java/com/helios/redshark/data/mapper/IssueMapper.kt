package com.helios.redshark.data.mapper

import com.helios.redshark.data.remote.firestore.dto.IssueDto
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssuePriority
import com.helios.redshark.domain.model.IssueStatus
import java.time.Instant
import java.util.UUID

fun IssueDto.toDomain(): Issue = Issue(
    id = UUID.fromString(id),
    ideaId = UUID.fromString(ideaId),
    authorId = authorId,
    assigneeId = assigneeId,
    title = title,
    description = description,
    status = runCatching { IssueStatus.valueOf(status) }.getOrDefault(IssueStatus.OPEN),
    priority = runCatching { IssuePriority.valueOf(priority) }.getOrDefault(IssuePriority.MEDIUM),
    createdAt = createdAt?.toDate()?.toInstant() ?: Instant.now(),
    updatedAt = updatedAt?.toDate()?.toInstant() ?: Instant.now(),
    deletedAt = deletedAt?.toDate()?.toInstant(),
)
