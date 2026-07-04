package com.helios.redshark.domain.model

// File nay mo ta du lieu nghiep vu va trang thai chinh cua ung dung.

import java.time.LocalDate

// Model du lieu nay giu cac truong can truyen giua cac lop.
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
