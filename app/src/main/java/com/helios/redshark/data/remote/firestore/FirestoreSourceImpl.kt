package com.helios.redshark.data.remote.firestore

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.firestore.dto.UserDto
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : FirestoreSource {

    private val users = firestore.collection("users")

    override suspend fun upsertUser(
        userId: String,
        email: String,
        displayName: String,
    ): Result<UserDto> {
        return try {
            val data = mapOf(
                "id" to userId,
                "email" to email,
                "displayName" to displayName,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            users.document(userId).set(data, SetOptions.merge()).await()
            Timber.d("Firestore: upsertUser uid=$userId")
            getUser(userId)
        } catch (e: Exception) {
            Timber.w(e, "Firestore: upsertUser failed uid=$userId")
            Result.Error(AppException.UnknownException(e.message ?: "Firestore upsert failed", e))
        }
    }

    override suspend fun getUser(userId: String): Result<UserDto> {
        return try {
            val doc = users.document(userId).get().await()
            if (!doc.exists()) {
                Result.Error(AppException.ServerException(404, "User not found: $userId"))
            } else {
                val dto = doc.toObject(UserDto::class.java)
                    ?: return Result.Error(AppException.UnknownException("Failed to parse user document"))
                Result.Success(dto.copy(id = doc.id))
            }
        } catch (e: Exception) {
            Timber.w(e, "Firestore: getUser failed uid=$userId")
            Result.Error(AppException.UnknownException(e.message ?: "Firestore get failed", e))
        }
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        bio: String?,
        skills: List<String>,
        avatarUrl: String?,
    ): Result<UserDto> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "skills" to skills,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            bio?.let { updates["bio"] = it }
            avatarUrl?.let { updates["avatarUrl"] = it }
            users.document(userId).update(updates).await()
            Timber.d("Firestore: updateProfile uid=$userId")
            getUser(userId)
        } catch (e: Exception) {
            Timber.w(e, "Firestore: updateProfile failed uid=$userId")
            Result.Error(AppException.UnknownException(e.message ?: "Firestore update failed", e))
        }
    }

    override suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Result<UserDto> {
        return try {
            users.document(userId).update(mapOf(
                "avatarUrl" to avatarUrl,
                "updatedAt" to FieldValue.serverTimestamp(),
            )).await()
            Timber.d("Firestore: updateAvatarUrl uid=$userId")
            getUser(userId)
        } catch (e: Exception) {
            Timber.w(e, "Firestore: updateAvatarUrl failed uid=$userId")
            Result.Error(AppException.UnknownException(e.message ?: "Firestore update failed", e))
        }
    }
}
