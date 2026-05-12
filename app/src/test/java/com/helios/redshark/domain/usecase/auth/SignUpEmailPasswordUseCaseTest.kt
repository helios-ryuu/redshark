package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SignUpEmailPasswordUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var checkUsernameAvailability: CheckUsernameAvailabilityUseCase
    private lateinit var useCase: SignUpEmailPasswordUseCase

    private val validDob = LocalDate.now().minusYears(20)
    private val testUser = User(
        id = "uid_789",
        email = "bob@example.com",
        displayName = "Bob",
        avatarUrl = null,
        bio = null,
        skills = emptyList(),
        username = "bob_dev",
        dateOfBirth = validDob,
        authProvider = "EMAIL",
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        checkUsernameAvailability = CheckUsernameAvailabilityUseCase(authRepository)
        useCase = SignUpEmailPasswordUseCase(authRepository, checkUsernameAvailability)
    }

    private fun stubUsernameAvailable(username: String) {
        coEvery { authRepository.checkUsernameAvailability(username) } returns Result.Success(true)
    }

    @Test
    fun `invoke returns Success when all inputs are valid`() = runTest {
        stubUsernameAvailable("bob_dev")
        coEvery { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) } returns Result.Success(testUser)

        val result = useCase("Bob", "bob_dev", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke returns ValidationError when displayName is too short`() = runTest {
        val result = useCase("Bo", "bob_dev", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when displayName exceeds 50 chars`() = runTest {
        val result = useCase("B".repeat(51), "bob_dev", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
    }

    @Test
    fun `invoke returns ValidationError when username has illegal chars`() = runTest {
        val result = useCase("Bob", "Bob Dev!", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when username is too short`() = runTest {
        val result = useCase("Bob", "ab", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
    }

    @Test
    fun `invoke returns ConflictException when username is taken`() = runTest {
        coEvery { authRepository.checkUsernameAvailability("taken") } returns Result.Success(false)

        val result = useCase("Bob", "taken", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ConflictException)
        coVerify(exactly = 0) { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when email is invalid`() = runTest {
        stubUsernameAvailable("bob_dev")

        val result = useCase("Bob", "bob_dev", "not-an-email", validDob, "Password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke propagates email ConflictException when email is already taken`() = runTest {
        stubUsernameAvailable("bob_dev")
        coEvery { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) } returns
            Result.Error(AppException.ConflictException("This email is already taken", "email"))

        val result = useCase("Bob", "bob_dev", "bob@example.com", validDob, "Password1")

        assertTrue(result is Result.Error)
        val exception = (result as Result.Error).exception
        assertTrue(exception is AppException.ConflictException)
        assertEquals("email", (exception as AppException.ConflictException).field)
    }

    @Test
    fun `invoke returns ValidationError when age is less than 13`() = runTest {
        stubUsernameAvailable("bob_dev")
        val underageDob = LocalDate.now().minusYears(12)

        val result = useCase("Bob", "bob_dev", "bob@example.com", underageDob, "Password1")

        assertTrue(result is Result.Error)
        val ex = (result as Result.Error).exception
        assertTrue(ex is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when password is too short`() = runTest {
        stubUsernameAvailable("bob_dev")

        val result = useCase("Bob", "bob_dev", "bob@example.com", validDob, "Pass1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when password has no uppercase`() = runTest {
        stubUsernameAvailable("bob_dev")

        val result = useCase("Bob", "bob_dev", "bob@example.com", validDob, "password1")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
    }

    @Test
    fun `invoke returns ValidationError when password has no digit`() = runTest {
        stubUsernameAvailable("bob_dev")

        val result = useCase("Bob", "bob_dev", "bob@example.com", validDob, "PasswordNoDigit")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
    }

    @Test
    fun `invoke accepts user exactly 13 years old`() = runTest {
        stubUsernameAvailable("bob_dev")
        val exactlyThirteen = LocalDate.now().minusYears(13)
        coEvery { authRepository.signUpEmailPassword(any(), any(), any(), any(), any()) } returns Result.Success(testUser)

        val result = useCase("Bob", "bob_dev", "bob@example.com", exactlyThirteen, "Password1")

        assertTrue(result is Result.Success)
    }
}
