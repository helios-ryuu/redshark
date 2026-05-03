package com.helios.redshark.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.AppException
import com.helios.redshark.core.NetworkChecker
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.remote.firestore.dto.IdeaDto
import com.helios.redshark.domain.model.CreateIdeaInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.UpdateIdeaInput
import com.helios.redshark.domain.repository.IdeaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdeaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val networkChecker: NetworkChecker,
) : IdeaRepository {

    private val ideas = firestore.collection("ideas")

    override fun getMyIdeas(): Flow<List<Idea>> {
        val uid = auth.currentUser?.uid ?: return callbackFlow {
            close(AppException.UnauthorizedException())
        }

        val authorFlow = callbackFlow<List<Idea>> {
            val reg = ideas
                .whereEqualTo("authorId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(AppException.NetworkException(error))
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents
                        ?.mapNotNull { doc -> doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain() }
                        ?.filter { it.deletedAt == null }
                        ?: emptyList())
                }
            awaitClose { reg.remove() }
        }

        val collabFlow = callbackFlow<List<Idea>> {
            val reg = ideas
                .whereArrayContains("collaboratorIds", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(AppException.NetworkException(error))
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.documents
                        ?.mapNotNull { doc -> doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain() }
                        ?.filter { it.deletedAt == null }
                        ?: emptyList())
                }
            awaitClose { reg.remove() }
        }

        return authorFlow.combine(collabFlow) { authored, collaborated ->
            (authored + collaborated).distinctBy { it.id }
                .sortedByDescending { it.createdAt }
        }
    }

    override suspend fun getIdeaDetail(id: UUID): Idea {
        return try {
            val doc = ideas.document(id.toString()).get().await()
            if (!doc.exists()) throw AppException.NotFoundException("Idea")
            val idea = doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
            // TC-C23: treat soft-deleted documents the same as missing
            if (idea.deletedAt != null) throw AppException.NotFoundException("Idea")
            idea
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun create(input: CreateIdeaInput): Idea {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        return try {
            val newId = UUID.randomUUID().toString()
            val data = mapOf(
                "authorId" to uid,
                "title" to input.title,
                "description" to input.description,
                "status" to IdeaStatus.ACTIVE.name,
                "tagIds" to input.tagIds.map { it.toString() },
                "collaboratorIds" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "deletedAt" to null,
            )
            ideas.document(newId).set(data).await()
            val doc = ideas.document(newId).get().await()
            doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun update(id: UUID, input: UpdateIdeaInput): Idea {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        return try {
            val updates = mapOf(
                "title" to input.title,
                "description" to input.description,
                "tagIds" to input.tagIds.map { it.toString() },
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            ideas.document(id.toString()).update(updates).await()
            val doc = ideas.document(id.toString()).get().await()
            doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun updateStatus(id: UUID, newStatus: IdeaStatus): Idea {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        return try {
            ideas.document(id.toString()).update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            val doc = ideas.document(id.toString()).get().await()
            doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun softDelete(id: UUID) {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        try {
            ideas.document(id.toString()).update(
                mapOf(
                    "deletedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    override suspend fun addCollaborator(id: UUID, collaboratorId: String): Idea {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        return try {
            ideas.document(id.toString()).update(
                mapOf(
                    "collaboratorIds" to FieldValue.arrayUnion(collaboratorId),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            val doc = ideas.document(id.toString()).get().await()
            doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }
}
