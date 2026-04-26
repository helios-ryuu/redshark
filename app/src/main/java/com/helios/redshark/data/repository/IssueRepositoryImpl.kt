package com.helios.redshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.error.AppException
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.dto.IssueDto
import com.helios.redshark.domain.model.CreateIssueInput
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssueStatus
import com.helios.redshark.domain.model.UpdateIssueInput
import com.helios.redshark.domain.repository.IssueRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssueRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : IssueRepository {

    private val issues = firestore.collection("issues")

    override fun getIssuesByIdea(ideaId: UUID): Flow<List<Issue>> = callbackFlow {
        val registration = issues
            .whereEqualTo("ideaId", ideaId.toString())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(IssueDto::class.java)?.copy(id = doc.id)?.toDomain()
                    }
                    ?.filter { it.deletedAt == null }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun getOpenIssuesFromOthers(): Flow<List<Issue>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        val registration = issues
            .whereEqualTo("status", IssueStatus.OPEN.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(IssueDto::class.java)?.copy(id = doc.id)?.toDomain()
                    }
                    ?.filter { it.authorId != uid && it.deletedAt == null }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getIssueDetail(id: UUID): Issue {
        return try {
            val doc = issues.document(id.toString()).get().await()
            if (!doc.exists()) throw AppException.NotFoundException("Issue")
            doc.toObject(IssueDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun countMyActiveIssues(): Int {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val snapshot = issues
                .whereEqualTo("authorId", uid)
                .whereIn("status", listOf(IssueStatus.OPEN.name, IssueStatus.IN_PROGRESS.name))
                .get()
                .await()
            snapshot.documents.count { it.getTimestamp("deletedAt") == null }
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun create(input: CreateIssueInput): Issue {
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val newId = UUID.randomUUID().toString()
            val data = mapOf(
                "ideaId" to input.ideaId.toString(),
                "authorId" to uid,
                "assigneeId" to input.assigneeId,
                "title" to input.title,
                "description" to input.description,
                "status" to IssueStatus.OPEN.name,
                "priority" to input.priority.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "deletedAt" to null,
            )
            issues.document(newId).set(data).await()
            val doc = issues.document(newId).get().await()
            doc.toObject(IssueDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun update(id: UUID, input: UpdateIssueInput): Issue {
        return try {
            val updates = mutableMapOf<String, Any?>(
                "title" to input.title,
                "description" to input.description,
                "priority" to input.priority.name,
                "assigneeId" to input.assigneeId,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            issues.document(id.toString()).update(updates).await()
            val doc = issues.document(id.toString()).get().await()
            doc.toObject(IssueDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun updateStatus(id: UUID, newStatus: IssueStatus): Issue {
        return try {
            issues.document(id.toString()).update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            val doc = issues.document(id.toString()).get().await()
            doc.toObject(IssueDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun softDelete(id: UUID) {
        try {
            issues.document(id.toString()).update(
                mapOf(
                    "deletedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }
}
