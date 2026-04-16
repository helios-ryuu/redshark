package com.helios.redshark.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.helios.redshark.domain.model.User

fun FirebaseUser.toDomain(): User = User(
    id = uid,
    email = email ?: "",
    displayName = displayName ?: "",
    avatarUrl = photoUrl?.toString(),
    bio = null,
    skills = emptyList(),
)
