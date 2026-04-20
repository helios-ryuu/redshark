package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface NotificationRepository {
    /** Emits a live list of notifications for the current user, newest first. */
    fun getMyNotifications(): Flow<List<Notification>>

    suspend fun create(input: CreateNotificationInput): Notification

    suspend fun markAsRead(id: UUID)
}
