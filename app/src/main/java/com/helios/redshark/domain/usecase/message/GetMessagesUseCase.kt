package com.helios.redshark.domain.usecase.message

import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(conversationId: UUID): Flow<List<Message>> =
        messageRepository.getMessages(conversationId)
}

