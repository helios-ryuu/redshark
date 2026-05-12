package com.helios.redshark.domain.repository

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User

interface ProfileRepository {
    suspend fun completeFirstProfile(userId: String, displayName: String): Result<User>
    suspend fun updateProfile(userId: String, displayName: String, bio: String?, skills: List<String>): Result<User>
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String): Result<String>
    suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Result<User>
    suspend fun getProfile(userId: String): Result<User>
    suspend fun getUsers(): Result<List<User>>
}
