package com.helios.redshark.domain.usecase.message

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

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
import java.util.UUID

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class FindOrCreateDirectConversationUseCaseTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var useCase: FindOrCreateDirectConversationUseCase

    private val peerId = "peer_123"
    private val testConversation = Conversation(
        id = UUID.randomUUID(),
        participantIds = listOf("me", peerId),
        lastMessageAt = null,
        lastMessagePreview = null,
        lastMessageSenderId = null,
        hasUnread = false,
        type = ConversationType.DIRECT
    )

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Before
    fun setUp() {
        messageRepository = mockk()
        useCase = FindOrCreateDirectConversationUseCase(messageRepository)
    }

    @Test
    fun `invoke returns existing conversation if found`() = runTest {
        coEvery { messageRepository.findDirectConversation(peerId) } returns testConversation

        val result = useCase(peerId)

        assertEquals(testConversation, result)
        coVerify(exactly = 0) { messageRepository.createDirectConversation(any()) }
    }

    @Test
    fun `invoke creates new conversation if none found`() = runTest {
        coEvery { messageRepository.findDirectConversation(peerId) } returns null
        coEvery { messageRepository.createDirectConversation(peerId) } returns testConversation

        val result = useCase(peerId)

        assertEquals(testConversation, result)
        coVerify(exactly = 1) { messageRepository.createDirectConversation(peerId) }
    }
}
