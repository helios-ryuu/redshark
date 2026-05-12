package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.AuthRepository
import javax.inject.Inject

class CheckUsernameAvailabilityUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    private val usernameRegex = Regex("^[a-z0-9._-]+$")

    suspend operator fun invoke(username: String): Result<Boolean> {
        val trimmed = username.trim()
        if (trimmed.length !in 3..30) {
            return Result.Error(
                AppException.ValidationException("username", "must be 3–30 characters")
            )
        }
        if (!usernameRegex.matches(trimmed)) {
            return Result.Error(
                AppException.ValidationException("username", "only lowercase letters, numbers, . _ - allowed")
            )
        }
        return authRepository.checkUsernameAvailability(trimmed)
    }
}
