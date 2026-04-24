package com.helios.redshark.data.remote.firestore.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ConversationDto(
    val id: String = "",
    val type: String = "DIRECT",
    val participantIds: List<String> = emptyList(),
    val directKey: String = "",
    val lastMessage: String? = null,
    val lastMessageAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
)

