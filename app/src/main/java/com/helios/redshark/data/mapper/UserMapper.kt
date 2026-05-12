package com.helios.redshark.data.mapper

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.helios.redshark.data.remote.firestore.dto.UserDto
import com.helios.redshark.domain.model.User
import java.time.LocalDate
import java.time.ZoneOffset

fun FirebaseUser.toDomain(): User = User(
    id = uid,
    email = email ?: "",
    displayName = displayName ?: "",
    avatarUrl = photoUrl?.toString(),
    bio = null,
    skills = emptyList(),
    authProvider = "GOOGLE",
)

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    bio = bio,
    skills = skills,
    username = username,
    dateOfBirth = dateOfBirth?.toLocalDate(),
    authProvider = authProvider,
)

private fun Timestamp.toLocalDate(): LocalDate =
    toDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate()

fun LocalDate.toTimestamp(): Timestamp =
    Timestamp(java.util.Date.from(atStartOfDay(ZoneOffset.UTC).toInstant()))
