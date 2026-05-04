package com.helios.redshark.domain.usecase.notification

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.model.NotificationTargetType
import com.helios.redshark.domain.model.NotificationType
import com.helios.redshark.domain.repository.IdeaRepository
import com.helios.redshark.domain.repository.NotificationRepository
import com.helios.redshark.domain.usecase.message.FindOrCreateDirectConversationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class AcceptCollabUseCaseTest {

    private lateinit var ideaRepository: IdeaRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase
    private lateinit var useCase: AcceptCollabUseCase

    private val currentUserId = "current_uid"
    private val actorId = "actor_uid"
    private val targetId = UUID.randomUUID()

    private val testNotification = Notification(
        id = UUID.randomUUID(),
        recipientId = currentUserId,
        actorId = actorId,
        type = NotificationType.COLLAB_REQUEST,
        targetType = NotificationTargetType.IDEA,
        targetId = targetId,
        message = "Wanna collab?",
        isRead = false,
        createdAt = Instant.now()
    )

    @Before
    fun setUp() {
        ideaRepository = mockk(relaxed = true)
        notificationRepository = mockk(relaxed = true)
        auth = mockk()
        findOrCreateDirectConversationUseCase = mockk(relaxed = true)
        useCase = AcceptCollabUseCase(
            ideaRepository,
            notificationRepository,
            auth,
            findOrCreateDirectConversationUseCase
        )

        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns currentUserId
        every { auth.currentUser } returns firebaseUser
    }

    @Test
    fun `invoke throws UnauthorizedException when user not logged in`() = runTest {
        every { auth.currentUser } returns null

        org.junit.Assert.assertThrows(AppException.UnauthorizedException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(testNotification) }
        }
    }

    @Test
    fun `invoke throws ValidationException when actorId is missing`() = runTest {
        val invalidNotification = testNotification.copy(actorId = null)

        org.junit.Assert.assertThrows(AppException.ValidationException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(invalidNotification) }
        }
    }

    @Test
    fun `invoke performs all required actions when valid`() = runTest {
        useCase(testNotification)

        coVerify {
            ideaRepository.addCollaborator(targetId, actorId)
            findOrCreateDirectConversationUseCase(actorId)
            notificationRepository.markAsRead(testNotification.id)
            notificationRepository.create(match {
                it.recipientId == actorId &&
                it.actorId == currentUserId &&
                it.type == NotificationType.COLLAB_ACCEPTED
            })
        }
    }
}
