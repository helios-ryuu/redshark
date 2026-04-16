package com.helios.redshark.domain.repository

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
}
