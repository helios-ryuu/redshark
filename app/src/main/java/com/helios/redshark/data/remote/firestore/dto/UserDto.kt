package com.helios.redshark.data.remote.firestore.dto

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserDto(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val skills: List<String> = emptyList(),
)
