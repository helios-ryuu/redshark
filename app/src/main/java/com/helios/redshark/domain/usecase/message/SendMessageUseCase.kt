package com.helios.redshark.domain.usecase.message

import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
import com.helios.redshark.domain.repository.MessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(input: SendMessageInput): Message {
        if (input.content.isBlank())
            throw AppException.ValidationException("Nội dung tin nhắn không được để trống.")
        if (input.content.length > 2000)
            throw AppException.ValidationException("Tin nhắn không vượt quá 2000 ký tự.")
        return messageRepository.sendMessage(input)
    }
}
