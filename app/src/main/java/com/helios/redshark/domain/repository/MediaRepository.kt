package com.helios.redshark.domain.repository

import com.helios.redshark.core.util.Result

interface MediaRepository {
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String): Result<String>
}
