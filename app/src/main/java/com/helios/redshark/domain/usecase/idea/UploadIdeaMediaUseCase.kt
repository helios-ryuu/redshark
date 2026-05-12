package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.MediaAttachment
import com.helios.redshark.domain.model.MediaType
import com.helios.redshark.domain.repository.MediaRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class UploadIdeaMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(
        ideaId: UUID,
        userId: String,
        bytes: ByteArray,
        mimeType: String,
        fileName: String?,
    ): Result<MediaAttachment> {
        val type = when (mimeType) {
            "image/jpeg", "image/png", "image/webp" -> MediaType.IMAGE
            "video/mp4", "video/webm" -> MediaType.VIDEO
            else -> return Result.Error(AppException.ValidationException("media", "Unsupported media format: $mimeType"))
        }
        val maxBytes = if (type == MediaType.IMAGE) MAX_IMAGE_BYTES else MAX_VIDEO_BYTES
        if (bytes.size > maxBytes) {
            return Result.Error(AppException.ValidationException("media", "Media file is too large"))
        }
        return when (val upload = mediaRepository.uploadIdeaMedia(ideaId.toString(), userId, bytes, mimeType)) {
            is Result.Success -> Result.Success(
                MediaAttachment(
                    id = UUID.randomUUID(),
                    url = upload.data,
                    type = type,
                    mimeType = mimeType,
                    fileName = fileName,
                    sizeBytes = bytes.size.toLong(),
                    createdBy = userId,
                    createdAt = Instant.now(),
                )
            )
            is Result.Error -> upload
            is Result.Loading -> upload
        }
    }

    private companion object {
        const val MAX_IMAGE_BYTES = 8 * 1024 * 1024
        const val MAX_VIDEO_BYTES = 50 * 1024 * 1024
    }
}
