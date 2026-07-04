package com.helios.redshark.data.remote.firestore.dto

// File nay giu cau truc document doc ghi voi Firestore.

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

// Model du lieu nay giu cac truong can truyen giua cac lop.
@IgnoreExtraProperties
data class MessageDto(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null,
)
