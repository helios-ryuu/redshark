package com.helios.redshark.data.repository

// File nay noi nghiep vu voi Firebase, cache hoac nguon du lieu ben ngoai.

import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.r2.R2Client
import com.helios.redshark.domain.repository.MediaRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val r2Client: R2Client,
) : MediaRepository {

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun uploadAvatar(
        userId: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): Result<String> {
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val key = "avatars/$userId.$extension"
        return r2Client.putObject(key, imageBytes, mimeType)
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun uploadIdeaMedia(
        ideaId: String,
        userId: String,
        bytes: ByteArray,
        mimeType: String,
    ): Result<String> {
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "video/mp4" -> "mp4"
            "video/webm" -> "webm"
            else -> "bin"
        }
        val key = "ideas/$ideaId/$userId-${UUID.randomUUID()}.$extension"
        return r2Client.putObject(key, bytes, mimeType)
    }
}
