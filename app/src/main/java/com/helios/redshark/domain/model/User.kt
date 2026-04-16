package com.helios.redshark.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val skills: List<String>,
)
