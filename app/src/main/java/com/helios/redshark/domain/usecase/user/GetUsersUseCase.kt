package com.helios.redshark.domain.usecase.user

import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<List<User>> = profileRepository.getUsers()
}
