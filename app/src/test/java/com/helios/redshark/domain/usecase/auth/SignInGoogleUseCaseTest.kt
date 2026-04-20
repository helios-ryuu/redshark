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

class SignInGoogleUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: SignInGoogleUseCase

    private val testUser = User(
        id = "uid_123",
        email = "test@example.com",
        displayName = "Test User",
        avatarUrl = null,
        bio = null,
        skills = emptyList(),
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        useCase = SignInGoogleUseCase(authRepository)
    }

    @Test
    fun `invoke returns Success when repository sign-in succeeds`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } returns Result.Success(testUser)

        val result = useCase("valid_id_token")

        assertTrue(result is Result.Success)
        assertEquals(testUser, (result as Result.Success).data)
        coVerify(exactly = 1) { authRepository.signInWithGoogle("valid_id_token") }
    }

    @Test
    fun `invoke returns Error when repository sign-in fails`() = runTest {
        val exception = AppException.AuthException("Firebase auth failed")
        coEvery { authRepository.signInWithGoogle(any()) } returns Result.Error(exception)

        val result = useCase("invalid_token")

        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }

    @Test
    fun `invoke passes idToken to repository unchanged`() = runTest {
        val idToken = "eyJhbGciOiJSUzI1NiJ9.test"
        coEvery { authRepository.signInWithGoogle(idToken) } returns Result.Success(testUser)

        useCase(idToken)

        coVerify { authRepository.signInWithGoogle(idToken) }
    }
}
