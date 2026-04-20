package com.helios.redshark.core.error

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import timber.log.Timber
import java.io.IOException

object ErrorMapper {
    private const val NETWORK_MESSAGE =
        "Không có kết nối mạng. Vui lòng kiểm tra kết nối và thử lại."

    fun map(throwable: Throwable): AppException {
        Timber.e(throwable)
        return when (throwable) {
            is AppException -> throwable
            is GetCredentialCancellationException ->
                AppException.UserCancelledException()
            is NoCredentialException ->
                AppException.NoCredentialException()
            is FirebaseNetworkException ->
                AppException.NetworkException(NETWORK_MESSAGE, throwable)
            is IOException ->
                AppException.NetworkException(NETWORK_MESSAGE, throwable)
            is FirebaseAuthException ->
                AppException.AuthException(throwable.message ?: "Firebase auth error", throwable)
            else ->
                AppException.UnknownException(throwable.message ?: "Unknown error", throwable)
        }
    }
}
