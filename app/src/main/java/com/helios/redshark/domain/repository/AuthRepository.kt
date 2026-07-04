package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface AuthRepository {
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    fun observeAuthState(): Flow<User?>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun signInWithGoogle(idToken: String): Result<User>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun signOut(): Result<Unit>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun getCurrentUser(): Result<User?>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun signUpEmailPassword(
        email: String,
        password: String,
        displayName: String,
        username: String,
        dateOfBirth: LocalDate,
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    ): Result<User>
    suspend fun signInEmailPassword(email: String, password: String): Result<User>
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun checkUsernameAvailability(username: String): Result<Boolean>
}
