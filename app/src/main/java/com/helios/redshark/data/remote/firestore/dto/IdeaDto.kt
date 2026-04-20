package com.helios.redshark.data.remote.firestore.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class IdeaDto(
    val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val description: String? = null,
    val status: String = "ACTIVE",
    val tagIds: List<String> = emptyList(),
    val collaboratorIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)
