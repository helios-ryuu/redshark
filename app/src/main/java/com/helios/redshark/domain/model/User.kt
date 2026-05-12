package com.helios.redshark.domain.model

import java.time.LocalDate

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val skills: List<String>,
    val username: String? = null,
    val dateOfBirth: LocalDate? = null,
    val authProvider: String = "GOOGLE",
)
