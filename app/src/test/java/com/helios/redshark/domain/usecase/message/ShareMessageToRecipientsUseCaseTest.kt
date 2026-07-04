package com.helios.redshark.domain.usecase.message

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.ConversationType
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class ShareMessageToRecipientsUseCaseTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var useCase: ShareMessageToRecipientsUseCase

    private val currentUserId = "current_uid"
    private val messageText = "Idea: Build RedShark"

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Before
    fun setUp() {
        auth = mockk()
        findOrCreateDirectConversationUseCase = mockk()
        sendMessageUseCase = mockk()
        useCase = ShareMessageToRecipientsUseCase(
            auth,
            findOrCreateDirectConversationUseCase,
            sendMessageUseCase,
        )

        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns currentUserId
        every { auth.currentUser } returns firebaseUser
    }

    @Test
    fun `invoke throws when user is not logged in`() = runTest {
        every { auth.currentUser } returns null

        assertThrows(AppException.UnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase(messageText, listOf("peer"))
            }
        }
    }

    @Test
    fun `invoke throws when recipients are empty after filtering self`() = runTest {
        assertThrows(AppException.ValidationException::class.java) {
            kotlinx.coroutines.runBlocking {
                useCase(messageText, listOf(currentUserId, " ", currentUserId))
            }
        }
    }

    @Test
    fun `invoke dedupes recipients and skips current user`() = runTest {
        val peerId = "peer_uid"
        val conversation = conversation(peerId)
        coEvery { findOrCreateDirectConversationUseCase(peerId) } returns conversation
        coEvery {
            sendMessageUseCase(SendMessageInput(conversation.id, messageText))
        } returns message(conversation.id)

        val result = useCase(messageText, listOf(peerId, currentUserId, peerId))

        assertEquals(1, result.sentCount)
        assertEquals(1, result.totalCount)
        assertEquals(emptyList<Any>(), result.failures)
        coVerify(exactly = 1) { findOrCreateDirectConversationUseCase(peerId) }
        coVerify(exactly = 0) { findOrCreateDirectConversationUseCase(currentUserId) }
    }

    @Test
    fun `invoke returns partial failure and continues sending`() = runTest {
        val okPeer = "ok_peer"
        val failedPeer = "failed_peer"
        val okConversation = conversation(okPeer)

        coEvery { findOrCreateDirectConversationUseCase(okPeer) } returns okConversation
        coEvery {
            sendMessageUseCase(SendMessageInput(okConversation.id, messageText))
        } returns message(okConversation.id)
        coEvery { findOrCreateDirectConversationUseCase(failedPeer) } throws AppException.NetworkException()

        val result = useCase(messageText, listOf(okPeer, failedPeer))

        assertEquals(1, result.sentCount)
        assertEquals(2, result.totalCount)
        assertEquals(listOf(failedPeer), result.failures.map { it.recipientUserId })
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    private fun conversation(peerId: String) = Conversation(
        id = UUID.randomUUID(),
        participantIds = listOf(currentUserId, peerId),
        lastMessageAt = null,
        lastMessagePreview = null,
        lastMessageSenderId = null,
        hasUnread = false,
        type = ConversationType.DIRECT,
    )

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    private fun message(conversationId: UUID) = Message(
        id = UUID.randomUUID(),
        conversationId = conversationId,
        senderId = currentUserId,
        content = messageText,
        createdAt = Instant.now(),
    )
}
