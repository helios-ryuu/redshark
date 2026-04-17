package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.MediaRepository
import javax.inject.Inject

private const val MAX_AVATAR_BYTES = 5 * 1024 * 1024 // 5 MB

class UploadAvatarUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(
        userId: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): Result<String> {
        if (imageBytes.size > MAX_AVATAR_BYTES) {
            return Result.Error(
                AppException.ValidationException("Avatar must be 5 MB or smaller")
            )
        }
        if (mimeType !in listOf("image/jpeg", "image/png", "image/webp")) {
            return Result.Error(
                AppException.ValidationException("Unsupported image format: $mimeType")
            )
        }
        return mediaRepository.uploadAvatar(userId, imageBytes, mimeType)
    }
}
