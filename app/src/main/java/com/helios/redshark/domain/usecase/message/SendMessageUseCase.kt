package com.helios.redshark.domain.usecase.message

import com.helios.redshark.core.AppException
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.repository.MessageRepository
import java.util.UUID
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: UUID, content: String): Message {
        val normalized = content.trim()
        if (normalized.isBlank()) {
            throw AppException.ValidationException("content", "Message content must not be empty.")
        }
        if (normalized.length > 2000) {
            throw AppException.ValidationException("content", "Message content must be at most 2000 characters.")
        }
        return messageRepository.sendMessage(conversationId, normalized)
    }
}

