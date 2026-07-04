package com.helios.redshark.domain.usecase.auth

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class CompleteFirstProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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
