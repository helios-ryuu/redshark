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
    val mediaAttachments: List<MediaAttachmentDto> = emptyList(),
    val upvoteCount: Long = 0,
    val commentCount: Long = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class MediaAttachmentDto(
    val id: String = "",
    val url: String = "",
    val type: String = "IMAGE",
    val mimeType: String = "",
    val fileName: String? = null,
    val sizeBytes: Long = 0,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
)
