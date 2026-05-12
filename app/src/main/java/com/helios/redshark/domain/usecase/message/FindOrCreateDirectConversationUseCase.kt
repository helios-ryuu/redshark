package com.helios.redshark.domain.usecase.message

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.repository.MessageRepository
import javax.inject.Inject

class FindOrCreateDirectConversationUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(peerId: String): Conversation =
        messageRepository.findDirectConversation(peerId)
            ?: messageRepository.createDirectConversation(peerId)
}
