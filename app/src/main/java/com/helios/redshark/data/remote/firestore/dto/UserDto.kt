package com.helios.redshark.data.remote.firestore.dto

// File nay giu cau truc document doc ghi voi Firestore.

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

// Model du lieu nay giu cac truong can truyen giua cac lop.
@IgnoreExtraProperties
data class UserDto(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val skills: List<String> = emptyList(),
    val username: String? = null,
    val dateOfBirth: Timestamp? = null,
    val authProvider: String = "GOOGLE",
)
