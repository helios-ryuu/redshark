package com.helios.redshark.domain.usecase.idea

import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.util.Result
import com.helios.redshark.domain.model.MediaType
import com.helios.redshark.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class UploadIdeaMediaUseCaseTest {

    private lateinit var mediaRepository: MediaRepository
    private lateinit var useCase: UploadIdeaMediaUseCase

    private val ideaId = UUID.randomUUID()

    @Before
    fun setUp() {
        mediaRepository = mockk()
        useCase = UploadIdeaMediaUseCase(mediaRepository)
    }

    @Test
    fun `invoke returns attachment for valid image upload`() = runTest {
        coEvery {
            mediaRepository.uploadIdeaMedia(ideaId.toString(), "uid_1", any(), "image/png")
        } returns Result.Success("https://cdn.example.com/idea.png")

        val result = useCase(ideaId, "uid_1", byteArrayOf(1, 2, 3), "image/png", "idea.png")

        assertTrue(result is Result.Success)
        val attachment = (result as Result.Success).data
        assertEquals(MediaType.IMAGE, attachment.type)
        assertEquals("https://cdn.example.com/idea.png", attachment.url)
    }

    @Test
    fun `invoke rejects unsupported mime type`() = runTest {
        val result = useCase(ideaId, "uid_1", byteArrayOf(1), "application/pdf", "file.pdf")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.ValidationException)
        coVerify(exactly = 0) { mediaRepository.uploadIdeaMedia(any(), any(), any(), any()) }
    }
}
