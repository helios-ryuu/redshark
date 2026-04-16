package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.User
import com.helios.redshark.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompleteFirstProfileUseCaseTest {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: CompleteFirstProfileUseCase

    private val userId = "uid_123"
    private val testUser = User(
        id = userId,
        email = "test@example.com",
        displayName = "Alice",
        avatarUrl = null,
        bio = null,
        skills = emptyList(),
    )

    @Before
    fun setUp() {
        profileRepository = mockk()
        useCase = CompleteFirstProfileUseCase(profileRepository)
    }

    @Test
    fun `invoke returns Success for valid displayName`() = runTest {
        coEvery { profileRepository.completeFirstProfile(userId, "Alice") } returns Result.Success(testUser)

        val result = useCase(userId, "Alice")

        assertTrue(result is Result.Success)
        assertEquals(testUser, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns ValidationError when displayName is too short`() = runTest {
        val result = useCase(userId, "Ab")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { profileRepository.completeFirstProfile(any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when displayName is empty`() = runTest {
        val result = useCase(userId, "")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
    }

    @Test
    fun `invoke returns ValidationError when displayName exceeds 50 chars`() = runTest {
        val longName = "A".repeat(51)

        val result = useCase(userId, longName)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { profileRepository.completeFirstProfile(any(), any()) }
    }

    @Test
    fun `invoke trims displayName before validation and save`() = runTest {
        coEvery { profileRepository.completeFirstProfile(userId, "Alice") } returns Result.Success(testUser)

        val result = useCase(userId, "  Alice  ")

        assertTrue(result is Result.Success)
        coVerify { profileRepository.completeFirstProfile(userId, "Alice") }
    }

    @Test
    fun `invoke accepts displayName of exactly 3 characters`() = runTest {
        val user3 = testUser.copy(displayName = "Abc")
        coEvery { profileRepository.completeFirstProfile(userId, "Abc") } returns Result.Success(user3)

        val result = useCase(userId, "Abc")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke accepts displayName of exactly 50 characters`() = runTest {
        val name50 = "A".repeat(50)
        val user50 = testUser.copy(displayName = name50)
        coEvery { profileRepository.completeFirstProfile(userId, name50) } returns Result.Success(user50)

        val result = useCase(userId, name50)

        assertTrue(result is Result.Success)
    }
}
