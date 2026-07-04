package com.helios.redshark.domain.usecase.message

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.repository.MessageRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class FindOrCreateDirectConversationUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(peerId: String): Conversation =
        messageRepository.findDirectConversation(peerId)
            ?: messageRepository.createDirectConversation(peerId)
}
