package com.helios.redshark.data.remote.firebase

// File nay dong goi cach goi dich vu ben ngoai.

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.helios.redshark.BuildConfig
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class GoogleSignInHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun requestGoogleIdToken(activityContext: Context): Result<String> {
        // Step 1: Try One Tap (accounts already on device)
        val oneTapResult = tryOneTap(activityContext)
        if (oneTapResult is Result.Success) return oneTapResult

        // Step 2: If no credential found, fallback to Sign In With Google (web-based dialog)
        if (oneTapResult is Result.Error && oneTapResult.exception is AppException.NoCredentialException) {
            Timber.d("No saved credential — falling back to Sign In With Google")
            return trySignInWithGoogle(activityContext)
        }

        return oneTapResult
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    private suspend fun tryOneTap(activityContext: Context): Result<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activityContext, request)
            val idToken = GoogleIdTokenCredential.createFrom(response.credential.data).idToken
            Timber.d("One Tap sign-in succeeded")
            Result.Success(idToken)
        } catch (e: GetCredentialCancellationException) {
            Result.Error(AppException.UserCancelledException())
        } catch (e: NoCredentialException) {
            Result.Error(AppException.NoCredentialException())
        } catch (e: Exception) {
            Timber.e(e, "One Tap sign-in failed")
            Result.Error(AppException.AuthException(e.message ?: "One Tap sign-in failed", e))
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    private suspend fun trySignInWithGoogle(activityContext: Context): Result<String> {
        return try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                BuildConfig.GOOGLE_WEB_CLIENT_ID
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val response = credentialManager.getCredential(activityContext, request)
            val idToken = GoogleIdTokenCredential.createFrom(response.credential.data).idToken
            Timber.d("Sign In With Google succeeded")
            Result.Success(idToken)
        } catch (e: GetCredentialCancellationException) {
            Result.Error(AppException.UserCancelledException())
        } catch (e: NoCredentialException) {
            Result.Error(AppException.NoCredentialException("No Google account found. Please add a Google account on this device."))
        } catch (e: Exception) {
            Timber.e(e, "Sign In With Google failed")
            Result.Error(AppException.AuthException(e.message ?: "Sign-in failed", e))
        }
    }
}
