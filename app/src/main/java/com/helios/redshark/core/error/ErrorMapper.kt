package com.helios.redshark.core.error

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuthException
import timber.log.Timber

object ErrorMapper {
    fun map(throwable: Throwable): AppException {
        Timber.e(throwable)
        return when (throwable) {
            is AppException -> throwable
            is GetCredentialCancellationException ->
                AppException.UserCancelledException()
            is NoCredentialException ->
                AppException.NoCredentialException()
            is FirebaseAuthException ->
                AppException.AuthException(throwable.message ?: "Firebase auth error", throwable)
            else ->
                AppException.UnknownException(throwable.message ?: "Unknown error", throwable)
        }
    }
}
