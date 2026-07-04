package com.helios.redshark.domain.usecase.message

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import com.helios.redshark.domain.repository.MessageRepository
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
) {
    suspend operator fun invoke(input: SendMessageInput): Message {
        if (input.content.isBlank())
            throw AppException.ValidationException("Nội dung tin nhắn không được để trống.")
        if (input.content.length > 2000)
            throw AppException.ValidationException("Tin nhắn không vượt quá 2000 ký tự.")
        return messageRepository.sendMessage(input)
    }
}
