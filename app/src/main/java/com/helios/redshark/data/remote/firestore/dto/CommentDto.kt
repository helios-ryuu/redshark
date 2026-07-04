package com.helios.redshark.data.remote.firestore.dto

// File nay giu cau truc document doc ghi voi Firestore.

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

// Model du lieu nay giu cac truong can truyen giua cac lop.
@IgnoreExtraProperties
data class CommentDto(
    val id: String = "",
    val ideaId: String = "",
    val authorId: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null,
)
