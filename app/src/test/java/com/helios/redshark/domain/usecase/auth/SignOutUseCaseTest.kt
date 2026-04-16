package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignOutUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignOutUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = SignOutUseCase(authRepository)
    }

    @Test
    fun `invoke returns Success when sign-out succeeds`() = runTest {
        coEvery { authRepository.signOut() } returns Result.Success(Unit)

        val result = useCase()

        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun `invoke returns Error when sign-out fails`() = runTest {
        val exception = AppException.AuthException("Sign-out failed")
        coEvery { authRepository.signOut() } returns Result.Error(exception)

        val result = useCase()

        assertTrue(result is Result.Error)
    }
}
