package com.helios.redshark.data.remote.firestore.dto

// File nay giu cau truc document doc ghi voi Firestore.

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

// Model du lieu nay giu cac truong can truyen giua cac lop.
@IgnoreExtraProperties
data class NotificationDto(
    val id: String = "",
    val recipientId: String = "",
    val actorId: String? = null,
    val type: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null,
)
