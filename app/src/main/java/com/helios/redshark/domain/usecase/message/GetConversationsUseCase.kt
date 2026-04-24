package com.helios.redshark.domain.usecase.message

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(): Flow<List<Conversation>> = messageRepository.getMyConversations()
}

