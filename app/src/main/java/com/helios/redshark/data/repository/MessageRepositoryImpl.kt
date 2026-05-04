package com.helios.redshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.error.AppException
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.dto.ConversationDto
import com.helios.redshark.data.remote.firestore.dto.MessageDto
import com.helios.redshark.domain.model.Conversation
import com.helios.redshark.domain.model.Message
import com.helios.redshark.domain.model.SendMessageInput
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

    override fun getConversations(): Flow<List<Conversation>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        val registration = conversations
            .whereArrayContains("participantIds", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ConversationDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun getMessages(conversationId: UUID): Flow<List<Message>> = callbackFlow {
        val registration = messages
            .whereEqualTo("conversationId", conversationId.toString())
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MessageDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(input: SendMessageInput): Message {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val newId = UUID.randomUUID().toString()
            val serverTs = FieldValue.serverTimestamp()
            val data = mapOf(
                "conversationId" to input.conversationId.toString(),
                "senderId" to uid,
                "content" to input.content,
                "createdAt" to serverTs,
            )
            messages.document(newId).set(data).await()
            conversations.document(input.conversationId.toString()).update(
                mapOf(
                    "lastMessageAt" to serverTs,
                    "lastMessagePreview" to input.content.take(80),
                    "lastMessageSenderId" to uid,
                    "hasUnread" to true,
                )
            ).await()
            val doc = messages.document(newId).get().await()
            doc.toObject(MessageDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun findDirectConversation(peerId: String): Conversation? {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val snapshot = conversations
                .whereEqualTo("type", "DIRECT")
                .whereArrayContains("participantIds", uid)
                .get().await()
            snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(ConversationDto::class.java)?.copy(id = doc.id)?.toDomain()
                }
                .firstOrNull { conv ->
                    conv.participantIds.size == 2 && peerId in conv.participantIds
                }
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun createDirectConversation(peerId: String): Conversation {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val newId = UUID.randomUUID().toString()
            val data = mapOf(
                "participantIds" to listOf(uid, peerId),
                "type" to "DIRECT",
                "lastMessageAt" to null,
                "lastMessagePreview" to null,
                "lastMessageSenderId" to null,
                "hasUnread" to false,
            )
            conversations.document(newId).set(data).await()
            val doc = conversations.document(newId).get().await()
            doc.toObject(ConversationDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun markConversationRead(conversationId: UUID) {
        try {
            conversations.document(conversationId.toString())
                .update("hasUnread", false)
                .await()
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }
}
