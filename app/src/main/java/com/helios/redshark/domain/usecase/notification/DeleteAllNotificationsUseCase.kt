package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

class DeleteAllNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke() {
        notificationRepository.deleteAll()
    }
}

