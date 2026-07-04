package com.helios.redshark.domain.usecase.auth

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class SignInEmailPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    private val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return Result.Error(AppException.ValidationException("email", "Email is required"))
        }
        if (!emailRegex.matches(trimmedEmail)) {
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
