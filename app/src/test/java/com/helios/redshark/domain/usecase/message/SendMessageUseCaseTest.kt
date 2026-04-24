package com.helios.redshark.domain.usecase.message

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.MessageDeliveryStatus
import com.helios.redshark.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class SendMessageUseCaseTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var useCase: SendMessageUseCase

    private val conversationId = UUID.randomUUID()
    private val sentMessage = Message(
        id = UUID.randomUUID(),
        conversationId = conversationId,
        senderId = "u1",
        content = "hello",
        createdAt = Instant.now(),
        status = MessageDeliveryStatus.SENT,
    )

    @Before
    fun setUp() {
        messageRepository = mockk()
        useCase = SendMessageUseCase(messageRepository)
    }

    @Test
    fun `invoke trims content and forwards to repository`() = runTest {
        coEvery { messageRepository.sendMessage(conversationId, "hello") } returns sentMessage

        val result = useCase(conversationId, "  hello  ")

        assertEquals(sentMessage, result)
        coVerify(exactly = 1) { messageRepository.sendMessage(conversationId, "hello") }
    }

    @Test
    fun `invoke throws validation when content is blank`() = runTest {
        val result = runCatching { useCase(conversationId, "   ") }

        assertTrue(result.exceptionOrNull() is AppException.ValidationException)
    }

    @Test
    fun `invoke throws validation when content exceeds 2000`() = runTest {
        val result = runCatching { useCase(conversationId, "a".repeat(2001)) }

        assertTrue(result.exceptionOrNull() is AppException.ValidationException)
    }
}

