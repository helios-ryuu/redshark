package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.domain.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(id: UUID) {
        notificationRepository.markAsRead(id)
    }
}

