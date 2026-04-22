package com.helios.redshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.helios.redshark.core.AppException
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.dto.ConversationDto
import com.helios.redshark.data.remote.firestore.dto.MessageDto
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.repository.MessageRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : MessageRepository {

    private val conversations = firestore.collection("conversations")
    private val messages = firestore.collection("messages")

    override fun getMyConversations(): Flow<List<Conversation>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        val registration = conversations
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(mapFirestoreError(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(ConversationDto::class.java)?.copy(id = doc.id)?.toDomain()
                        }.getOrNull()
                    }
                    ?.sortedByDescending { it.lastMessageAt }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun getMessages(conversationId: UUID): Flow<List<Message>> = callbackFlow {
        val registration = messages
            .whereEqualTo("conversationId", conversationId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(mapFirestoreError(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(MessageDto::class.java)?.copy(id = doc.id)?.toDomain()
                        }.getOrNull()
                    }
                    ?.sortedBy { it.createdAt }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun findOrCreateDirectConversation(peerId: String): Conversation {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        if (peerId == uid) {
            throw AppException.ValidationException("peerId", "Không thể tạo hội thoại với chính bạn.")
        }
        val directKey = listOf(uid, peerId).sorted().joinToString("_")
        val conversationId = UUID.nameUUIDFromBytes(directKey.toByteArray()).toString()
        try {
            firestore.runTransaction { transaction ->
                val conversationRef = conversations.document(conversationId)
                val existing = transaction.get(conversationRef)
                if (!existing.exists()) {
                    val data = mapOf(
                        "type" to "DIRECT",
                        "participantIds" to listOf(uid, peerId).sorted(),
                        "directKey" to directKey,
                        "lastMessage" to null,
                        "lastMessageAt" to null,
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                    transaction.set(conversationRef, data)
                }
            }.await()
        } catch (e: AppException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            throw mapFirestoreError(e)
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }

        return getConversationById(conversationId)
    }

    override suspend fun sendMessage(conversationId: UUID, content: String): Message {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        val messageId = UUID.randomUUID().toString()
        val conversationRef = conversations.document(conversationId.toString())
        val messageRef = messages.document(messageId)

        try {
            firestore.runTransaction { transaction ->
                val conversation = transaction.get(conversationRef)
                if (!conversation.exists()) throw AppException.NotFoundException("Conversation")

                val participants = conversation.get("participantIds") as? List<*>
                if (participants?.contains(uid) != true) {
                    throw AppException.UnauthorizedException()
                }

                transaction.set(
                    messageRef,
                    mapOf(
                        "conversationId" to conversationId.toString(),
                        "senderId" to uid,
                        "content" to content,
                        "status" to "SENT",
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                )
                transaction.update(
                    conversationRef,
                    mapOf(
                        "lastMessage" to content,
                        "lastMessageAt" to FieldValue.serverTimestamp(),
                    )
                )
            }.await()
        } catch (e: AppException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            throw mapFirestoreError(e)
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }

        return getMessageById(messageId)
    }

    private suspend fun getConversationById(id: String): Conversation {
        val doc = conversations.document(id).get().await()
        if (!doc.exists()) throw AppException.NotFoundException("Conversation")
        return runCatching {
            doc.toObject(ConversationDto::class.java)?.copy(id = doc.id)?.toDomain()
        }.getOrNull()
            ?: throw AppException.UnknownException()
    }

    private suspend fun getMessageById(id: String): Message {
        val doc = messages.document(id).get().await()
        if (!doc.exists()) throw AppException.NotFoundException("Message")
        return runCatching {
            doc.toObject(MessageDto::class.java)?.copy(id = doc.id)?.toDomain()
        }.getOrNull()
            ?: throw AppException.UnknownException()
    }

    private fun mapFirestoreError(error: FirebaseFirestoreException): AppException {
        return when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                AppException.RemoteException(
                    "Firestore denied access to Messages. Please deploy firestore.rules.",
                    error,
                )

            FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                AppException.RemoteException(
                    "Firestore is missing required indexes for Messages. Please deploy firestore.indexes.json.",
                    error,
                )

            else -> AppException.NetworkException(error)
        }
    }
}

