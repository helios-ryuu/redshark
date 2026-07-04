package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface NotificationRepository {
    /** Emits a live list of notifications for the current user, newest first. */
    fun getMyNotifications(): Flow<List<Notification>>

    /** Emits unread notification count for badge display. */
    fun getUnreadCount(): Flow<Int>

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun create(input: CreateNotificationInput): Notification

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun markAsRead(id: UUID)

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun deleteAll()
}
