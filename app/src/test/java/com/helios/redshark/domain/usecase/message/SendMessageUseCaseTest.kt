package com.helios.redshark.domain.usecase.message

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import com.helios.redshark.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class SendMessageUseCaseTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var useCase: SendMessageUseCase

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Before
    fun setUp() {
        messageRepository = mockk()
        useCase = SendMessageUseCase(messageRepository)
    }

    @Test
    fun `invoke throws ValidationException when content is blank`() = runTest {
        val input = SendMessageInput(
            conversationId = UUID.randomUUID(),
            content = "  "
        )

        val exception = org.junit.Assert.assertThrows(AppException.ValidationException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(input) }
        }
        assertEquals("Nội dung tin nhắn không được để trống.", exception.message)
    }

    @Test
    fun `invoke throws ValidationException when content exceeds 2000 chars`() = runTest {
        val longContent = "a".repeat(2001)
        val input = SendMessageInput(
            conversationId = UUID.randomUUID(),
            content = longContent
        )

        val exception = org.junit.Assert.assertThrows(AppException.ValidationException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(input) }
        }
        assertEquals("Tin nhắn không vượt quá 2000 ký tự.", exception.message)
    }

    @Test
    fun `invoke calls repository when input is valid`() = runTest {
        val input = SendMessageInput(
            conversationId = UUID.randomUUID(),
            content = "Hello world"
        )
        val expectedMessage = Message(
            id = UUID.randomUUID(),
            conversationId = input.conversationId,
            senderId = "user1",
            content = input.content,
            createdAt = Instant.now()
        )
        coEvery { messageRepository.sendMessage(input) } returns expectedMessage

        val result = useCase(input)

        assertEquals(expectedMessage, result)
    }
}
