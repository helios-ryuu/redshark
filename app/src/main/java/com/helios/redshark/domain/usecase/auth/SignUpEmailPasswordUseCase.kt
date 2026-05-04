package com.helios.redshark.domain.usecase.auth

import android.util.Patterns
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import java.time.LocalDate
import javax.inject.Inject

class SignUpEmailPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val checkUsernameAvailability: CheckUsernameAvailabilityUseCase,
) {
    private val passwordUppercase = Regex(".*[A-Z].*")
    private val passwordDigit = Regex(".*[0-9].*")

    suspend operator fun invoke(
        displayName: String,
        username: String,
        email: String,
        dateOfBirth: LocalDate,
        password: String,
    ): Result<User> {
        val trimmedName = displayName.trim()
        if (trimmedName.length !in 3..50) {
            return Result.Error(
                AppException.ValidationException("displayName", "Display name must be 3–50 characters")
            )
        }

        val trimmedUsername = username.trim()
        when (val check = checkUsernameAvailability(trimmedUsername)) {
            is Result.Error -> return check
            is Result.Success -> if (!check.data) {
                return Result.Error(AppException.ConflictException("This username is already taken"))
            }
            else -> Unit
        }

        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return Result.Error(AppException.ValidationException("email", "Enter a valid email address"))
        }

        val minAge = LocalDate.now().minusYears(13)
        if (dateOfBirth.isAfter(minAge)) {
            return Result.Error(AppException.ValidationException("dateOfBirth", "You must be at least 13 years old"))
        }

        if (password.length < 8) {
            return Result.Error(
                AppException.ValidationException("password", "Password must be at least 8 characters")
            )
        }
        if (!passwordUppercase.matches(password) || !passwordDigit.matches(password)) {
            return Result.Error(
                AppException.ValidationException("password", "Must include uppercase letter and digit")
            )
        }

        return authRepository.signUpEmailPassword(
            email = trimmedEmail,
            password = password,
            displayName = trimmedName,
            username = trimmedUsername,
            dateOfBirth = dateOfBirth,
        )
    }
}
