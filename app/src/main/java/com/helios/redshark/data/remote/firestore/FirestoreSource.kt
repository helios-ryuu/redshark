package com.helios.redshark.data.remote.firestore

// File nay dong goi cach goi dich vu ben ngoai.

import com.helios.redshark.core.util.Result
import com.helios.redshark.data.remote.firestore.dto.UserDto

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface FirestoreSource {
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun upsertUser(userId: String, email: String, displayName: String): Result<UserDto>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun upsertEmailUser(
        userId: String,
        email: String,
        displayName: String,
        username: String,
        dateOfBirth: com.google.firebase.Timestamp,
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    ): Result<UserDto>
    suspend fun getUser(userId: String): Result<UserDto>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun getUsers(): Result<List<UserDto>>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun updateProfile(
        userId: String,
        displayName: String,
        bio: String?,
        skills: List<String>,
        avatarUrl: String?,
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    ): Result<UserDto>
    suspend fun updateAvatarUrl(userId: String, avatarUrl: String): Result<UserDto>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
}
