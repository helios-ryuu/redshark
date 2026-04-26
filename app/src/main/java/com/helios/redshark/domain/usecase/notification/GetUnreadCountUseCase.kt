package com.helios.redshark.domain.usecase.notification

import com.helios.redshark.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUnreadCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    operator fun invoke(): Flow<Int> =
        notificationRepository.getMyNotifications().map { list -> list.count { !it.isRead } }
}
