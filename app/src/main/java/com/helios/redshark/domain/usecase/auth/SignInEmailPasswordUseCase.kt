package com.helios.redshark.domain.usecase.auth

import android.util.Patterns
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import javax.inject.Inject

class SignInEmailPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.Error(AppException.ValidationException("email", "Email is required"))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.Error(AppException.ValidationException("email", "Enter a valid email address"))
        }
        if (password.isBlank()) {
            return Result.Error(AppException.ValidationException("password", "Password is required"))
        }
        if (password.length < 8) {
            return Result.Error(AppException.ValidationException("password", "Password must be at least 8 characters"))
        }
        return authRepository.signInEmailPassword(trimmedEmail, password)
    }
}
