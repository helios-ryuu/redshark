package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.Instant
import java.util.UUID

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class Comment(
    val id: UUID,
    val ideaId: UUID,
    val authorId: String,
    val content: String,     // 1–1000 chars
    val createdAt: Instant
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class CreateCommentInput(
    val ideaId: UUID,
    val content: String
)
