package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckUsernameAvailabilityUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: CheckUsernameAvailabilityUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = CheckUsernameAvailabilityUseCase(authRepository)
    }

    @Test
    fun `invoke returns Success true when username is available`() = runTest {
        coEvery { authRepository.checkUsernameAvailability("alice.dev") } returns Result.Success(true)

        val result = useCase("alice.dev")

        assertTrue(result is Result.Success)
        assertEquals(true, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns Success false when username is taken`() = runTest {
        coEvery { authRepository.checkUsernameAvailability("taken_user") } returns Result.Success(false)

        val result = useCase("taken_user")

        assertTrue(result is Result.Success)
        assertEquals(false, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns ValidationError when username is too short`() = runTest {
        val result = useCase("ab")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.checkUsernameAvailability(any()) }
    }

    @Test
    fun `invoke returns ValidationError when username is too long`() = runTest {
        val result = useCase("a".repeat(31))

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.checkUsernameAvailability(any()) }
    }

    @Test
    fun `invoke returns ValidationError when username contains uppercase`() = runTest {
        val result = useCase("AliceUser")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.checkUsernameAvailability(any()) }
    }

    @Test
    fun `invoke returns ValidationError when username contains illegal chars`() = runTest {
        val result = useCase("alice user!")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.checkUsernameAvailability(any()) }
    }

    @Test
    fun `invoke accepts username with valid special chars dot underscore hyphen`() = runTest {
        coEvery { authRepository.checkUsernameAvailability("al.ice_dev-1") } returns Result.Success(true)

        val result = useCase("al.ice_dev-1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke trims whitespace before validation`() = runTest {
        coEvery { authRepository.checkUsernameAvailability("alicedev") } returns Result.Success(true)

        useCase("  alicedev  ")

        coVerify { authRepository.checkUsernameAvailability("alicedev") }
    }
}
