package com.helios.redshark.data.remote.firebase

// File nay dong goi cach goi dich vu ben ngoai.

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

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class FirebaseAuthSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        trySend(firebaseAuth.currentUser)
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun signUpEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.Error(AppException.AuthException("Sign-up succeeded but user is null"))
            Timber.d("Firebase email sign-up success: uid=${user.uid}")
            Result.Success(user)
        } catch (e: Exception) {
            Timber.e(e, "Firebase email sign-up failed")
            Result.Error(ErrorMapper.map(e))
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun signInEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.Error(AppException.AuthException("Sign-in succeeded but user is null"))
            Timber.d("Firebase email sign-in success: uid=${user.uid}")
            Result.Success(user)
        } catch (e: Exception) {
            Timber.e(e, "Firebase email sign-in failed")
            Result.Error(ErrorMapper.map(e))
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(ErrorMapper.map(e))
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
