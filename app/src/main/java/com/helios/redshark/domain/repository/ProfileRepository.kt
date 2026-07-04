package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface ProfileRepository {
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun completeFirstProfile(userId: String, displayName: String): Result<User>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun updateProfile(userId: String, displayName: String, bio: String?, skills: List<String>): Result<User>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, mimeType: String): Result<String>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Result<User>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun getProfile(userId: String): Result<User>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun getUsers(): Result<List<User>>
}
