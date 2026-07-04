package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.Instant
import java.util.UUID

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class IdeaStatus { ACTIVE, CLOSED, CANCELLED }

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class MediaType { IMAGE, VIDEO }

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class MediaAttachment(
    val id: UUID,
    val url: String,
    val type: MediaType,
    val mimeType: String,
    val fileName: String?,
    val sizeBytes: Long,
    val createdBy: String,
    val createdAt: Instant,
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class Idea(
    val id: UUID,
    val authorId: String,
    val title: String,                   // 3–120 chars
    val description: String?,            // ≤ 5000 chars; nullable
    val status: IdeaStatus,
    val tagIds: List<UUID>,
    val collaboratorIds: List<String>,
    val mediaAttachments: List<MediaAttachment> = emptyList(),
    val upvoteCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?              // null = not soft-deleted
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class CreateIdeaInput(
    val title: String,
    val description: String?,
    val tagIds: List<UUID> = emptyList(),
    val mediaAttachments: List<MediaAttachment> = emptyList(),
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class UpdateIdeaInput(
    val title: String,
    val description: String?,
    val tagIds: List<UUID> = emptyList(),
    val mediaAttachments: List<MediaAttachment> = emptyList(),
)
