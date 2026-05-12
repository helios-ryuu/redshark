package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignInEmailPasswordUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignInEmailPasswordUseCase

    private val testUser = User(
        id = "uid_456",
        email = "alice@example.com",
        displayName = "Alice",
        avatarUrl = null,
        bio = null,
        skills = emptyList(),
        authProvider = "EMAIL",
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = SignInEmailPasswordUseCase(authRepository)
    }

    @Test
    fun `invoke returns Success when credentials are valid`() = runTest {
        coEvery { authRepository.signInEmailPassword(any(), any()) } returns Result.Success(testUser)

        val result = useCase("alice@example.com", "Password1")

        assertTrue(result is Result.Success)
        assertEquals(testUser, (result as Result.Success).data)
        coVerify(exactly = 1) { authRepository.signInEmailPassword("alice@example.com", "Password1") }
    }

    @Test
    fun `invoke returns ValidationError when email is blank`() = runTest {
        val result = useCase("", "Password1")

        assertTrue(result is Result.Error)
        val ex = (result as Result.Error).exception
        assertTrue(ex is AppException.ValidationException)
        assertEquals("email", (ex as AppException.ValidationException).field)
        coVerify(exactly = 0) { authRepository.signInEmailPassword(any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when email format is invalid`() = runTest {
        val result = useCase("not-an-email", "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signInEmailPassword(any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when password is blank`() = runTest {
        val result = useCase("alice@example.com", "")

        assertTrue(result is Result.Error)
        val ex = (result as Result.Error).exception
        assertTrue(ex is AppException.ValidationException)
        assertEquals("password", (ex as AppException.ValidationException).field)
        coVerify(exactly = 0) { authRepository.signInEmailPassword(any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when password is shorter than 8 chars`() = runTest {
        val result = useCase("alice@example.com", "Pass1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signInEmailPassword(any(), any()) }
    }

    @Test
    fun `invoke trims email before validation and passing to repository`() = runTest {
        coEvery { authRepository.signInEmailPassword("alice@example.com", any()) } returns Result.Success(testUser)

        useCase("  alice@example.com  ", "Password1")

        coVerify { authRepository.signInEmailPassword("alice@example.com", "Password1") }
    }

    @Test
    fun `invoke propagates repository error`() = runTest {
        val exception = AppException.AuthException("Wrong password")
        coEvery { authRepository.signInEmailPassword(any(), any()) } returns Result.Error(exception)

        val result = useCase("alice@example.com", "WrongPass1")

        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
}
