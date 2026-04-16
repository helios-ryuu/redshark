package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.signOut()
}
