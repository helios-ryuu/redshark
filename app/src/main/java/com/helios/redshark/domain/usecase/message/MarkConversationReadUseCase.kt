package com.helios.redshark.domain.usecase.message

import com.helios.redshark.domain.repository.MessageRepository
import java.util.UUID
import javax.inject.Inject

class MarkConversationReadUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(conversationId: UUID) {
        messageRepository.markConversationRead(conversationId)
    }
}

