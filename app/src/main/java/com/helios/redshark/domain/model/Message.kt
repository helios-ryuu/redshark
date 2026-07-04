package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.Instant
import java.util.UUID

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class Message(
    val id: UUID,
    val conversationId: UUID,
    val senderId: String,
    val content: String,
    val createdAt: Instant,
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class SendMessageInput(
    val conversationId: UUID,
    val content: String,
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class ShareMessageFailure(
    val recipientUserId: String,
    val message: String,
)

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class ShareMessageResult(
    val sentCount: Int,
    val totalCount: Int,
    val failures: List<ShareMessageFailure>,
) {
    val isComplete: Boolean = failures.isEmpty() && sentCount == totalCount
}
