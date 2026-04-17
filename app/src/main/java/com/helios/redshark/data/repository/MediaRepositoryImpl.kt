package com.helios.redshark.data.repository

import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.r2.R2Client
import com.helios.redshark.domain.repository.MediaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val r2Client: R2Client,
) : MediaRepository {

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
}
