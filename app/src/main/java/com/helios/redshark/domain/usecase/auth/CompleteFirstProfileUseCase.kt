package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import javax.inject.Inject

class CompleteFirstProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(userId: String, displayName: String): Result<User> {
        val trimmed = displayName.trim()
        if (trimmed.length !in 3..50) {
            return Result.Error(
                AppException.ValidationException("displayName must be 3–50 characters")
            )
        }
        return profileRepository.completeFirstProfile(userId, trimmed)
    }
}
