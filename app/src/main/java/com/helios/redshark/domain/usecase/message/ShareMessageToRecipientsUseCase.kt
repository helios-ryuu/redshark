package com.helios.redshark.domain.usecase.message

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.google.firebase.auth.FirebaseAuth
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.SendMessageInput
import com.helios.redshark.domain.model.ShareMessageFailure
import com.helios.redshark.domain.model.ShareMessageResult
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class ShareMessageToRecipientsUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val findOrCreateDirectConversationUseCase: FindOrCreateDirectConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(
        messageText: String,
        recipientUserIds: Collection<String>,
    ): ShareMessageResult {
        val currentUserId = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        if (messageText.isBlank()) {
            throw AppException.ValidationException("Nội dung chia sẻ không được để trống.")
        }

        val recipients = recipientUserIds
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != currentUserId }
            .distinct()
            .toList()

        if (recipients.isEmpty()) {
            throw AppException.ValidationException("Chọn ít nhất một người nhận.")
        }

        var sentCount = 0
        val failures = mutableListOf<ShareMessageFailure>()
        recipients.forEach { recipientId ->
            runCatching {
                val conversation = findOrCreateDirectConversationUseCase(recipientId)
                sendMessageUseCase(SendMessageInput(conversation.id, messageText))
            }.onSuccess {
                sentCount += 1
            }.onFailure { error ->
                failures += ShareMessageFailure(
                    recipientUserId = recipientId,
                    message = error.message ?: "Không gửi được tin nhắn.",
                )
            }
        }

        return ShareMessageResult(
            sentCount = sentCount,
            totalCount = recipients.size,
            failures = failures,
        )
    }
}
