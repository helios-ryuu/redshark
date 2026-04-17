package com.helios.redshark.data.remote.firestore

import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.firestore.dto.UserDto

interface FirestoreSource {
    suspend fun upsertUser(userId: String, email: String, displayName: String): Result<UserDto>
    suspend fun getUser(userId: String): Result<UserDto>
    suspend fun updateProfile(
        userId: String,
        displayName: String,
        bio: String?,
        skills: List<String>,
        avatarUrl: String?,
    ): Result<UserDto>
    suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Result<UserDto>
}
