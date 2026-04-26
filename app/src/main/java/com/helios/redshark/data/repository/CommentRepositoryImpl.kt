package com.helios.redshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.error.AppException
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.dto.CommentDto
import com.helios.redshark.domain.model.Comment
import com.helios.redshark.domain.model.CreateCommentInput
import com.helios.redshark.domain.repository.CommentRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : CommentRepository {

    private val comments = firestore.collection("comments")

    override fun getCommentsByIdea(ideaId: UUID): Flow<List<Comment>> = callbackFlow {
        val registration = comments
            .whereEqualTo("ideaId", ideaId.toString())
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(CommentDto::class.java)?.copy(id = doc.id)?.toDomain()
                    } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun create(input: CreateCommentInput): Comment {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val newId = UUID.randomUUID().toString()
            val data = mapOf(
                "ideaId" to input.ideaId.toString(),
                "authorId" to uid,
                "content" to input.content,
                "createdAt" to FieldValue.serverTimestamp(),
            )
            comments.document(newId).set(data).await()
            val doc = comments.document(newId).get().await()
            doc.toObject(CommentDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }
}
