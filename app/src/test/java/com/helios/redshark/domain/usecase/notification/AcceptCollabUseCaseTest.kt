package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.ConversationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.MessageRepository
import com.helios.redshark.domain.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class AcceptCollabUseCaseTest {

    private lateinit var ideaRepository: IdeaRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var useCase: AcceptCollabUseCase
    private lateinit var expectedConversation: Conversation

    private val ideaId = UUID.randomUUID()
    private val requestNotification = Notification(
        id = UUID.randomUUID(),
        recipientId = "owner",
        actorId = "requester",
        type = NotificationType.COLLAB_REQUEST,
        targetType = NotificationTargetType.IDEA,
        targetId = ideaId,
        message = "xin tham gia",
        isRead = false,
        createdAt = Instant.now(),
    )

    @Before
    fun setUp() {
        ideaRepository = mockk()
        notificationRepository = mockk()
        messageRepository = mockk()
        useCase = AcceptCollabUseCase(ideaRepository, notificationRepository, messageRepository)

        coEvery { ideaRepository.addCollaborator(any(), any()) } returns Idea(
            id = ideaId,
            authorId = "owner",
            title = "title",
            description = null,
            status = IdeaStatus.ACTIVE,
            tagIds = emptyList(),
            collaboratorIds = listOf("requester"),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            deletedAt = null,
        )
        coEvery { notificationRepository.markAsRead(any()) } returns Unit
        coEvery { notificationRepository.create(any<CreateNotificationInput>()) } returns requestNotification
        expectedConversation = Conversation(
            id = UUID.randomUUID(),
            type = ConversationType.DIRECT,
            participantIds = listOf("owner", "requester"),
            directKey = "owner_requester",
            lastMessage = null,
            lastMessageAt = null,
            createdAt = Instant.now(),
        )
        coEvery { messageRepository.findOrCreateDirectConversation(any()) } returns expectedConversation
    }

    @Test
    fun `invoke adds collaborator emits accepted notification and returns conversation`() = runTest {
        val inputSlot = slot<CreateNotificationInput>()

        val conversation = useCase(requestNotification)

        coVerify(exactly = 1) { ideaRepository.addCollaborator(ideaId, "requester") }
        coVerify(exactly = 1) { messageRepository.findOrCreateDirectConversation("requester") }
        coVerify(exactly = 1) { notificationRepository.markAsRead(requestNotification.id) }
        coVerify(exactly = 1) { notificationRepository.create(capture(inputSlot)) }
        assertEquals("requester", inputSlot.captured.recipientId)
        assertEquals("owner", inputSlot.captured.actorId)
        assertEquals(NotificationType.COLLAB_ACCEPTED, inputSlot.captured.type)
        assertEquals(NotificationTargetType.IDEA, inputSlot.captured.targetType)
        assertEquals(ideaId, inputSlot.captured.targetId)
        assertEquals(expectedConversation, conversation)
    }
}

