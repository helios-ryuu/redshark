package com.helios.redshark.data.remote.firestore.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ConversationDto(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessageAt: Timestamp? = null,
    val lastMessagePreview: String? = null,
    val lastMessageSenderId: String? = null,
    val hasUnread: Boolean = false,
    val type: String = "DIRECT",
)
