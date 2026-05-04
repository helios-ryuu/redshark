package com.helios.redshark.data.repository

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.data.local.datastore.UserPreferences
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.mapper.toTimestamp
import com.helios.redshark.data.remote.firestore.FirestoreSource
import com.helios.redshark.data.remote.firebase.FirebaseAuthSource
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
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

    override suspend fun signUpEmailPassword(
        email: String,
        password: String,
        displayName: String,
        username: String,
        dateOfBirth: LocalDate,
    ): Result<User> {
        return when (val result = firebaseAuthSource.signUpEmailPassword(email, password)) {
            is Result.Success -> {
                val firebaseUser = result.data
                val upsertResult = firestoreSource.upsertEmailUser(
                    userId = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    username = username,
                    dateOfBirth = dateOfBirth.toTimestamp(),
                )
                val user = when (upsertResult) {
                    is Result.Success -> upsertResult.data.toDomain()
                    else -> firebaseUser.toDomain().copy(
                        displayName = displayName,
                        username = username,
                        dateOfBirth = dateOfBirth,
                        authProvider = "EMAIL",
                    )
                }
                userPreferences.saveUser(user.id, user.displayName)
                Timber.d("Auth repo: email sign-up success uid=${user.id}")
                Result.Success(user)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun signInEmailPassword(email: String, password: String): Result<User> {
        return when (val result = firebaseAuthSource.signInEmailPassword(email, password)) {
            is Result.Success -> {
                val firebaseUser = result.data
                val firestoreResult = firestoreSource.getUser(firebaseUser.uid)
                val user = when (firestoreResult) {
                    is Result.Success -> firestoreResult.data.toDomain()
                    else -> firebaseUser.toDomain().copy(authProvider = "EMAIL")
                }
                userPreferences.saveUser(user.id, user.displayName)
                Timber.d("Auth repo: email sign-in success uid=${user.id}")
                Result.Success(user)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
        return try {
            val available = firestoreSource.isUsernameAvailable(username)
            Result.Success(available)
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.message ?: "Failed to check username", e))
        }
    }
}
