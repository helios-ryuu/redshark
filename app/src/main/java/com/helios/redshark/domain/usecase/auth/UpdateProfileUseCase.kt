package com.helios.redshark.domain.usecase.auth

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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
        val trimmedBio = bio?.trim()?.ifBlank { null }
        if (trimmedBio != null && trimmedBio.length > 280) {
            return Result.Error(
                AppException.ValidationException("bio must be 280 characters or fewer")
            )
        }
        return profileRepository.updateProfile(userId, trimmedName, trimmedBio, skills)
    }
}
