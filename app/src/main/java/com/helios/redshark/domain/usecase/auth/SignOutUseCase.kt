package com.helios.redshark.domain.usecase.auth

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.AuthRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.signOut()
}
