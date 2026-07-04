package com.helios.redshark.domain.usecase.auth

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

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

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class UpdateProfileUseCaseTest {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var useCase: UpdateProfileUseCase

    private val userId = "uid_123"
    private val testUser = User(
        id = userId,
        email = "test@example.com",
        displayName = "Alice",
        avatarUrl = null,
        bio = "Hello world",
        skills = listOf("Android"),
    )

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Before
    fun setUp() {
        profileRepository = mockk()
        useCase = UpdateProfileUseCase(profileRepository)
    }

    @Test
    fun `invoke returns Success for valid inputs`() = runTest {
        coEvery {
            profileRepository.updateProfile(userId, "Alice", "Hello world", listOf("Android"))
        } returns Result.Success(testUser)

        val result = useCase(userId, "Alice", "Hello world", listOf("Android"))

        assertTrue(result is Result.Success)
        assertEquals(testUser, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns ValidationError when displayName is too short`() = runTest {
        val result = useCase(userId, "Ab", null, emptyList())

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { profileRepository.updateProfile(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError when displayName exceeds 50 chars`() = runTest {
        val result = useCase(userId, "A".repeat(51), null, emptyList())

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
    }

    @Test
    fun `invoke returns ValidationError when bio exceeds 280 chars`() = runTest {
        val result = useCase(userId, "Alice", "B".repeat(281), emptyList())

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { profileRepository.updateProfile(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke trims displayName before validation and save`() = runTest {
        coEvery {
            profileRepository.updateProfile(userId, "Alice", null, emptyList())
        } returns Result.Success(testUser)

        val result = useCase(userId, "  Alice  ", null, emptyList())

        assertTrue(result is Result.Success)
        coVerify { profileRepository.updateProfile(userId, "Alice", null, emptyList()) }
    }


    @Test
    fun `invoke converts blank bio to null so profile can be cleared`() = runTest {
        coEvery {
            profileRepository.updateProfile(userId, "Alice", null, emptyList())
        } returns Result.Success(testUser.copy(bio = null, skills = emptyList()))

        val result = useCase(userId, "Alice", "   ", emptyList())

        assertTrue(result is Result.Success)
        coVerify { profileRepository.updateProfile(userId, "Alice", null, emptyList()) }
    }

    @Test
    fun `invoke accepts null bio`() = runTest {
        coEvery {
            profileRepository.updateProfile(userId, "Alice", null, emptyList())
        } returns Result.Success(testUser)

        val result = useCase(userId, "Alice", null, emptyList())

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke accepts bio of exactly 280 chars`() = runTest {
        val bio280 = "B".repeat(280)
        coEvery {
            profileRepository.updateProfile(userId, "Alice", bio280, emptyList())
        } returns Result.Success(testUser)

        val result = useCase(userId, "Alice", bio280, emptyList())

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke passes skills to repository unchanged`() = runTest {
        val skills = listOf("Android", "iOS", "Web")
        coEvery {
            profileRepository.updateProfile(userId, "Alice", null, skills)
        } returns Result.Success(testUser)

        useCase(userId, "Alice", null, skills)

        coVerify { profileRepository.updateProfile(userId, "Alice", null, skills) }
    }
}
