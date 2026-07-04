package com.helios.redshark.data.repository

// File nay noi nghiep vu voi Firebase, cache hoac nguon du lieu ben ngoai.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.local.datastore.UserPreferences
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.FirestoreSource
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val firestoreSource: FirestoreSource,
    private val userPreferences: UserPreferences,
) : ProfileRepository {

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun completeFirstProfile(userId: String, displayName: String): Result<User> {
        return try {
            val result = firestoreSource.upsertUser(userId, "", displayName)
            when (result) {
                is Result.Success -> {
                    val user = result.data.toDomain()
                    userPreferences.saveUser(user.id, user.displayName)
                    Timber.d("Profile completed for uid=$userId displayName=$displayName")
                    Result.Success(user)
                }
                is Result.Error -> {
                    userPreferences.saveUser(userId, displayName)
                    Timber.w("Firestore unavailable — persisted locally: uid=$userId")
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
                }
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.message ?: "Failed to complete profile", e))
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        bio: String?,
        skills: List<String>,
    ): Result<User> {
        return try {
            val result = firestoreSource.updateProfile(
                userId = userId,
                displayName = displayName,
                bio = bio,
                skills = skills,
                avatarUrl = null,
            )
            when (result) {
                is Result.Success -> {
                    val user = result.data.toDomain()
                    userPreferences.saveUser(user.id, user.displayName)
                    Result.Success(user)
                }
                is Result.Error -> result

                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.message ?: "Failed to update profile", e))
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun uploadAvatar(
        userId: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): Result<String> {
        return Result.Error(AppException.StorageException("Use UploadAvatarUseCase to upload avatar"))
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Result<User> {
        return when (val result = firestoreSource.updateAvatarUrl(userId, avatarUrl)) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun getProfile(userId: String): Result<User> {
        return when (val result = firestoreSource.getUser(userId)) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun getUsers(): Result<List<User>> {
        return when (val result = firestoreSource.getUsers()) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }
}
