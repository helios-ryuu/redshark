package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = authRepository.observeAuthState()
}
