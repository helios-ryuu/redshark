package com.helios.redshark.data.repository

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.local.datastore.UserPreferences
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.FirestoreSource
import com.helios.redshark.data.remote.firebase.FirebaseAuthSource
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthSource: FirebaseAuthSource,
    private val firestoreSource: FirestoreSource,
    private val userPreferences: UserPreferences,
) : AuthRepository {

    override fun observeAuthState(): Flow<User?> =
        firebaseAuthSource.observeAuthState().map { firebaseUser ->
            firebaseUser?.toDomain()
        }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return when (val result = firebaseAuthSource.signInWithGoogle(idToken)) {
            is Result.Success -> {
                val firebaseUser = result.data
                val baseUser = firebaseUser.toDomain()
                val upsertResult = firestoreSource.upsertUser(
                    userId = baseUser.id,
                    email = baseUser.email,
                    displayName = baseUser.displayName,
                )
                val user = when (upsertResult) {
                    is Result.Success -> upsertResult.data.toDomain()
                    else -> baseUser
                }
                userPreferences.saveUser(user.id, user.displayName)
                Timber.d("Auth repo: sign-in success uid=${user.id}")
                Result.Success(user)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return when (val result = firebaseAuthSource.signOut()) {
            is Result.Success -> {
                userPreferences.clear()
                Result.Success(Unit)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val user = firebaseAuthSource.getCurrentUser()?.toDomain()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.message ?: "Failed to get current user", e))
        }
    }
}
