package com.helios.redshark.domain.model

import java.time.Instant
import java.util.UUID

enum class IssueStatus { OPEN, IN_PROGRESS, CLOSED, CANCELLED }

enum class IssuePriority { LOW, MEDIUM, HIGH }

data class Issue(
    val id: UUID,
    val ideaId: UUID,
    val authorId: String,
    val assigneeId: String?,             // nullable: issue may be unassigned
    val title: String,                   // 3–120 chars
    val description: String?,            // ≤ 5000 chars; nullable
    val status: IssueStatus,
    val priority: IssuePriority,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?              // null = not soft-deleted
)

/**
 * Valid transitions:
 *   OPEN        → IN_PROGRESS, CANCELLED
 *   IN_PROGRESS → CLOSED
 *   CLOSED      → (terminal)
 *   CANCELLED   → (terminal)
 */
val ISSUE_STATE_MACHINE: Map<IssueStatus, Set<IssueStatus>> = mapOf(
    IssueStatus.OPEN        to setOf(IssueStatus.IN_PROGRESS, IssueStatus.CANCELLED),
    IssueStatus.IN_PROGRESS to setOf(IssueStatus.CLOSED),
    IssueStatus.CLOSED      to emptySet(),
    IssueStatus.CANCELLED   to emptySet()
)

data class CreateIssueInput(
    val ideaId: UUID,
    val title: String,
    val description: String?,
    val priority: IssuePriority = IssuePriority.MEDIUM,
    val assigneeId: String? = null
)

data class UpdateIssueInput(
    val title: String,
    val description: String?,
    val priority: IssuePriority,
    val assigneeId: String?
)
