package com.helios.redshark.domain.usecase.message

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.ConversationType
import com.helios.redshark.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class FindOrCreateDirectConversationUseCaseTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var useCase: FindOrCreateDirectConversationUseCase

    @Before
    fun setUp() {
        messageRepository = mockk()
        useCase = FindOrCreateDirectConversationUseCase(messageRepository)
    }

    @Test
    fun `invoke delegates to repository and returns conversation`() = runTest {
        val conversation = Conversation(
            id = UUID.randomUUID(),
            type = ConversationType.DIRECT,
            participantIds = listOf("u1", "u2"),
            directKey = "u1_u2",
            lastMessage = null,
            lastMessageAt = null,
            createdAt = Instant.now(),
        )
        coEvery { messageRepository.findOrCreateDirectConversation("u2") } returns conversation

        val result = useCase("u2")

        assertEquals(conversation, result)
        coVerify(exactly = 1) { messageRepository.findOrCreateDirectConversation("u2") }
    }
}

