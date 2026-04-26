package com.helios.redshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.error.AppException
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.dto.NotificationDto
import com.helios.redshark.domain.model.CreateNotificationInput
import com.helios.redshark.domain.model.Notification
import com.helios.redshark.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : NotificationRepository {

    private val notifications = firestore.collection("notifications")

    override fun getMyNotifications(): Flow<List<Notification>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        val registration = notifications
            .whereEqualTo("recipientId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(NotificationDto::class.java)?.copy(id = doc.id)?.toDomain()
                    } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun getUnreadCount(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        val registration = notifications
            .whereEqualTo("recipientId", uid)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun create(input: CreateNotificationInput): Notification {
        return try {
            val newId = UUID.randomUUID().toString()
            val data = mapOf(
                "recipientId" to input.recipientId,
                "actorId" to input.actorId,
                "type" to input.type.name,
                "targetType" to input.targetType.name,
                "targetId" to input.targetId.toString(),
                "message" to input.message,
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp(),
            )
            notifications.document(newId).set(data).await()
            val doc = notifications.document(newId).get().await()
            doc.toObject(NotificationDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun markAsRead(id: UUID) {
        try {
            notifications.document(id.toString()).update("isRead", true).await()
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }
}
