package com.helios.redshark.core.error

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    // Auth errors
    class AuthException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    class UserCancelledException(message: String = "User cancelled the operation") :
        AppException(message)

    class NoCredentialException(message: String = "No credential available") :
        AppException(message)

    // Network / server errors
    class NetworkException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    class ServerException(val code: Int, message: String) :
        AppException(message)

    // Validation errors
    class ValidationException(message: String) :
        AppException(message)

    // Storage errors
    class StorageException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    // Generic
    class UnknownException(message: String = "Unknown error", cause: Throwable? = null) :
        AppException(message, cause)
}
