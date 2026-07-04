package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.Instant
import java.util.UUID

// Enum nay gioi han cac gia tri hop le de tranh dung chuoi tuy tien.
enum class ConversationType { DIRECT }

// Model du lieu nay giu cac truong can truyen giua cac lop.
data class Conversation(
    val id: UUID,
    val participantIds: List<String>,
    val lastMessageAt: Instant?,
    val lastMessagePreview: String?,
    val lastMessageSenderId: String?,
    val hasUnread: Boolean,
    val type: ConversationType,
)
