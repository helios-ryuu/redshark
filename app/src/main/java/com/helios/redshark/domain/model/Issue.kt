package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.Instant
import java.util.UUID

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class IssueStatus { OPEN, IN_PROGRESS, CLOSED, CANCELLED }

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class IssuePriority { LOW, MEDIUM, HIGH }

// Model du lieu nay giu cac truong can truyen giua cac lop.
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

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class CreateIssueInput(
    val ideaId: UUID,
    val title: String,
    val description: String?,
    val priority: IssuePriority = IssuePriority.MEDIUM,
    val assigneeId: String? = null
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class UpdateIssueInput(
    val title: String,
    val description: String?,
    val priority: IssuePriority,
    val assigneeId: String?
)
