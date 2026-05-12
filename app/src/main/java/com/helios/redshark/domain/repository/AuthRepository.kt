package com.helios.redshark.domain.repository

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AuthRepository {
    fun observeAuthState(): Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun signUpEmailPassword(
        email: String,
        password: String,
        displayName: String,
        username: String,
        dateOfBirth: LocalDate,
    ): Result<User>
    suspend fun signInEmailPassword(email: String, password: String): Result<User>
    suspend fun checkUsernameAvailability(username: String): Result<Boolean>
}
