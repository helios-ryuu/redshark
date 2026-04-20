package com.helios.redshark.domain.usecase.auth

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UploadAvatarUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var useCase: UploadAvatarUseCase

    private val userId = "uid_123"
    private val smallBytes = ByteArray(100 * 1024) // 100 KB
    private val largeBytes = ByteArray(6 * 1024 * 1024) // 6 MB

    @Before
    fun setUp() {
        mediaRepository = mockk()
        useCase = UploadAvatarUseCase(mediaRepository)
    }

    @Test
    fun `invoke returns Success for valid jpeg`() = runTest {
        val url = "https://r2.example.com/avatars/uid_123.jpg"
        coEvery { mediaRepository.uploadAvatar(userId, smallBytes, "image/jpeg") } returns Result.Success(url)

        val result = useCase(userId, smallBytes, "image/jpeg")

        assertTrue(result is Result.Success)
        assertEquals(url, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns Success for valid png`() = runTest {
        coEvery { mediaRepository.uploadAvatar(userId, smallBytes, "image/png") } returns Result.Success("url")

        val result = useCase(userId, smallBytes, "image/png")

        assertTrue(result is Result.Success)
    }

    @Test
    fun `invoke returns ValidationError when image exceeds 5MB`() = runTest {
        val result = useCase(userId, largeBytes, "image/jpeg")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { mediaRepository.uploadAvatar(any(), any(), any()) }
    }

    @Test
    fun `invoke returns ValidationError for unsupported mime type`() = runTest {
        val result = useCase(userId, smallBytes, "image/gif")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { mediaRepository.uploadAvatar(any(), any(), any()) }
    }

    @Test
    fun `invoke passes exact bytes to repository`() = runTest {
        coEvery { mediaRepository.uploadAvatar(userId, smallBytes, "image/jpeg") } returns Result.Success("url")

        useCase(userId, smallBytes, "image/jpeg")

        coVerify { mediaRepository.uploadAvatar(userId, smallBytes, "image/jpeg") }
    }

    @Test
    fun `invoke propagates StorageException from repository`() = runTest {
        val exception = AppException.StorageException("Upload failed")
        coEvery { mediaRepository.uploadAvatar(userId, smallBytes, "image/jpeg") } returns Result.Error(exception)

        val result = useCase(userId, smallBytes, "image/jpeg")

        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }

    @Test
    fun `invoke accepts image exactly at 5MB limit`() = runTest {
        val exactly5MB = ByteArray(5 * 1024 * 1024)
        coEvery { mediaRepository.uploadAvatar(userId, exactly5MB, "image/jpeg") } returns Result.Success("url")

        val result = useCase(userId, exactly5MB, "image/jpeg")

        assertTrue(result is Result.Success)
    }
}
