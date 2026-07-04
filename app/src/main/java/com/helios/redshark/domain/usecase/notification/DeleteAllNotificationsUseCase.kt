package com.helios.redshark.domain.usecase.notification

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.repository.NotificationRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class DeleteAllNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke() {
        notificationRepository.deleteAll()
    }
}

