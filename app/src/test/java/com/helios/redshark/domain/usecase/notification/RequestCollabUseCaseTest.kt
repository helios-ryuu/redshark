package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
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

class RequestCollabUseCaseTest {

    private lateinit var ideaRepository: IdeaRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var useCase: RequestCollabUseCase

    private val ideaId = UUID.randomUUID()
    private val requesterId = "requester"
    private val idea = Idea(
        id = ideaId,
        authorId = "owner",
        title = "title",
        description = null,
        status = IdeaStatus.ACTIVE,
        tagIds = emptyList(),
        collaboratorIds = emptyList(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        deletedAt = null,
    )

    @Before
    fun setUp() {
        ideaRepository = mockk()
        notificationRepository = mockk()
        useCase = RequestCollabUseCase(ideaRepository, notificationRepository)

        coEvery { ideaRepository.getIdeaDetail(ideaId) } returns idea
        coEvery { notificationRepository.create(any<CreateNotificationInput>()) } returns Notification(
            id = UUID.randomUUID(),
            recipientId = idea.authorId,
            actorId = requesterId,
            type = NotificationType.COLLAB_REQUEST,
            targetType = NotificationTargetType.IDEA,
            targetId = ideaId,
            message = "Co nguoi dung muon tham gia idea cua ban.",
            isRead = false,
            createdAt = Instant.now(),
        )
    }

    @Test
    fun `invoke creates collab request notification for idea author`() = runTest {
        val inputSlot = slot<CreateNotificationInput>()

        useCase(ideaId, requesterId)

        coVerify(exactly = 1) { ideaRepository.getIdeaDetail(ideaId) }
        coVerify(exactly = 1) { notificationRepository.create(capture(inputSlot)) }
        assertEquals("owner", inputSlot.captured.recipientId)
        assertEquals(requesterId, inputSlot.captured.actorId)
        assertEquals(NotificationType.COLLAB_REQUEST, inputSlot.captured.type)
        assertEquals(NotificationTargetType.IDEA, inputSlot.captured.targetType)
        assertEquals(ideaId, inputSlot.captured.targetId)
    }

    @Test(expected = AppException.ValidationException::class)
    fun `invoke rejects request when requester is author`() = runTest {
        useCase(ideaId, "owner")
    }
}

