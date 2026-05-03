package com.helios.redshark.domain.usecase.message

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

class SendMessageUseCaseTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setUp() {
        messageRepository = mockk()
        useCase = SendMessageUseCase(messageRepository)
    }

    @Test
    fun `invoke throws ValidationException when content is blank`() = runTest {
        val input = SendMessageInput(
            conversationId = UUID.randomUUID(),
            senderId = "user1",
            content = "  "
        )

        val exception = assertThrows(AppException.ValidationException::class.java) {
            runTest { useCase(input) }
        }
        assertEquals("Nội dung tin nhắn không được để trống.", exception.message)
    }

    @Test
    fun `invoke throws ValidationException when content exceeds 2000 chars`() = runTest {
        val longContent = "a".repeat(2001)
        val input = SendMessageInput(
            conversationId = UUID.randomUUID(),
            senderId = "user1",
            content = longContent
        )

        val exception = assertThrows(AppException.ValidationException::class.java) {
            runTest { useCase(input) }
        }
        assertEquals("Tin nhắn không vượt quá 2000 ký tự.", exception.message)
    }

    @Test
    fun `invoke calls repository when input is valid`() = runTest {
        val input = SendMessageInput(
            conversationId = UUID.randomUUID(),
            senderId = "user1",
            content = "Hello world"
        )
        val expectedMessage = Message(
            id = UUID.randomUUID(),
            conversationId = input.conversationId,
            senderId = input.senderId,
            content = input.content,
            createdAt = Instant.now()
        )
        coEvery { messageRepository.sendMessage(input) } returns expectedMessage

        val result = useCase(input)

        assertEquals(expectedMessage, result)
    }
}
