package com.helios.redshark.data.remote.firestore.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

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
