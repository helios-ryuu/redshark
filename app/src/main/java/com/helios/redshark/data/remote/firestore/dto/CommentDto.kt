package com.helios.redshark.data.remote.firestore.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CommentDto(
    val id: String = "",
    val ideaId: String = "",
    val authorId: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null,
)
