package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.core.util.Result

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface MediaRepository {
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String): Result<String>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun uploadIdeaMedia(
        ideaId: String,
        userId: String,
        bytes: ByteArray,
        mimeType: String,
    ): Result<String>
}
