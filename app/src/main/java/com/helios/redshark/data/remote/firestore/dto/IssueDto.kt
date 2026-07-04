package com.helios.redshark.data.remote.firestore.dto

// File nay giu cau truc document doc ghi voi Firestore.

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

// Model du lieu nay giu cac truong can truyen giua cac lop.
@IgnoreExtraProperties
data class IssueDto(
    val id: String = "",
    val ideaId: String = "",
    val authorId: String = "",
    val assigneeId: String? = null,
    val title: String = "",
    val description: String? = null,
    val status: String = "OPEN",
    val priority: String = "MEDIUM",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val deletedAt: Timestamp? = null,
)
