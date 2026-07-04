package com.helios.redshark.core.error

// File nay chuan hoa loi de cac tang xu ly thong nhat.

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import timber.log.Timber
import java.io.IOException

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
object ErrorMapper {
    private const val NETWORK_MESSAGE =
        "Không có kết nối mạng. Vui lòng kiểm tra kết nối và thử lại."

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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
                when (throwable.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" ->
                        AppException.ConflictException("This email is already taken", "email")
                    else -> AppException.AuthException(throwable.message ?: "Firebase auth error", throwable)
                }
            else ->
                AppException.UnknownException(throwable.message ?: "Unknown error", throwable)
        }
    }
}
