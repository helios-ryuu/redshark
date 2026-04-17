package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(
        userId: String,
        displayName: String,
        bio: String?,
        skills: List<String>,
    ): Result<User> {
        val trimmedName = displayName.trim()
        if (trimmedName.length !in 3..50) {
            return Result.Error(
                AppException.ValidationException("displayName must be 3–50 characters")
            )
        }
        val trimmedBio = bio?.trim()
        if (trimmedBio != null && trimmedBio.length > 280) {
            return Result.Error(
                AppException.ValidationException("bio must be 280 characters or fewer")
            )
        }
        return profileRepository.updateProfile(userId, trimmedName, trimmedBio, skills)
    }
}
