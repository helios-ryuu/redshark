package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Flow<List<Notification>> = notificationRepository.getMyNotifications()
}
