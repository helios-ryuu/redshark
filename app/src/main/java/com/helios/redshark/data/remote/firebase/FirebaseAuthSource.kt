package com.helios.redshark.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.error.ErrorMapper
import com.helios.redshark.core.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
                ?: return Result.Error(AppException.AuthException("Sign-in succeeded but user is null"))
            Timber.d("Firebase sign-in success: uid=${user.uid}")
            Result.Success(user)
        } catch (e: Exception) {
            Timber.e(e, "Firebase sign-in failed")
            Result.Error(ErrorMapper.map(e))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e))
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
