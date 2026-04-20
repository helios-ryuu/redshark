package com.helios.redshark.data.repository

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.local.datastore.UserPreferences
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences,
) : ProfileRepository {

    override suspend fun completeFirstProfile(userId: String, displayName: String): Result<User> {
        // TODO: call Firebase Data Connect UpsertUser mutation when FDC SDK is generated
        return try {
            userPreferences.saveUser(userId, displayName)
            Timber.d("Profile completed for uid=$userId displayName=$displayName")
            Result.Success(
                User(
                    id = userId,
                    email = "",
                    displayName = displayName,
                    avatarUrl = null,
                    bio = null,
                    skills = emptyList(),
                )
            )
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.message ?: "Failed to complete profile", e))
        }
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        bio: String?,
        skills: List<String>,
    ): Result<User> {
        // TODO: call Firebase Data Connect UpdateProfile mutation
        return try {
            userPreferences.saveUser(userId, displayName)
            Result.Success(
                User(
                    id = userId,
                    email = "",
                    displayName = displayName,
                    avatarUrl = null,
                    bio = bio,
                    skills = skills,
                )
            )
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.message ?: "Failed to update profile", e))
        }
    }

    override suspend fun uploadAvatar(
        userId: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): Result<String> {
        // TODO: implement R2 upload with OkHttp + AWS SigV4
        return Result.Error(AppException.StorageException("R2 upload not yet implemented"))
    }

    override suspend fun getProfile(userId: String): Result<User> {
        // TODO: call Firebase Data Connect GetMe query
        return Result.Error(AppException.UnknownException("FDC integration pending"))
    }
}
