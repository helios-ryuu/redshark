package com.helios.redshark.data.repository

// File nay noi nghiep vu voi Firebase, cache hoac nguon du lieu ben ngoai.

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.error.AppException
import com.helios.redshark.core.NetworkChecker
import com.helios.redshark.data.mapper.toDomain
import com.helios.redshark.data.mapper.toFirestoreMap
import com.helios.redshark.data.remote.firestore.dto.IdeaDto
import com.helios.redshark.domain.model.CreateIdeaInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaReaction
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.MediaAttachment
import com.helios.redshark.domain.model.UpdateIdeaInput
import com.helios.redshark.domain.repository.IdeaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Singleton
class IdeaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val networkChecker: NetworkChecker,
) : IdeaRepository {

    private val ideas = firestore.collection("ideas")

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override fun getAllIdeas(): Flow<List<Idea>> = callbackFlow {
        trySend(emptyList())
        val registration = ideas
            .whereEqualTo("deletedAt", null)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                    }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override fun getMyIdeas(): Flow<List<Idea>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        trySend(emptyList())

        var authoredIdeas = emptyMap<String, Idea>()
        var collaboratedIdeas = emptyMap<String, Idea>()

        // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
        fun parseSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot?): Map<String, Idea> =
            snapshot?.documents
                ?.mapNotNull { doc ->
                    doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                }
                ?.filter { it.deletedAt == null }
                ?.associateBy { it.id.toString() }
                ?: emptyMap()

        // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
        fun emitCombined() {
            trySend(
                (authoredIdeas + collaboratedIdeas)
                    .values
                    .sortedByDescending { it.createdAt }
            )
        }

        val authorRegistration = ideas
            .whereEqualTo("authorId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                authoredIdeas = parseSnapshot(snapshot)
                emitCombined()
            }

        val collaboratorRegistration = ideas
            .whereArrayContains("collaboratorIds", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(AppException.NetworkException(error))
                    return@addSnapshotListener
                }
                collaboratedIdeas = parseSnapshot(snapshot)
                emitCombined()
            }

        awaitClose {
            authorRegistration.remove()
            collaboratorRegistration.remove()
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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
                "mediaAttachments" to input.mediaAttachments.map { it.toFirestoreMap() },
                "upvoteCount" to 0,
                "commentCount" to 0,
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun updateMediaAttachments(id: UUID, mediaAttachments: List<MediaAttachment>): Idea {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        return try {
            ideas.document(id.toString()).update(
                mapOf(
                    "mediaAttachments" to mediaAttachments.map { it.toFirestoreMap() },
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun addCollaborator(ideaId: UUID, userId: String): Idea {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        return try {
            ideas.document(ideaId.toString()).update(
                mapOf(
                    "collaboratorIds" to FieldValue.arrayUnion(userId),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            ).await()
            val doc = ideas.document(ideaId.toString()).get().await()
            doc.toObject(IdeaDto::class.java)?.copy(id = doc.id)?.toDomain()
                ?: throw AppException.UnknownException()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override suspend fun setReaction(ideaId: UUID, reaction: IdeaReaction) {
        if (!networkChecker.isOnline()) throw AppException.NetworkException()
        val uid = auth.currentUser?.uid ?: throw AppException.UnauthorizedException()
        val ideaRef = ideas.document(ideaId.toString())
        val reactionRef = ideaRef.collection("reactions").document(uid)

        try {
            firestore.runTransaction { transaction ->
                val ideaSnapshot = transaction.get(ideaRef)
                if (!ideaSnapshot.exists()) throw AppException.NotFoundException("Idea")

                val currentCount = (ideaSnapshot.getLong("upvoteCount") ?: 0L).toInt()
                val currentReaction = reactionFromString(
                    transaction.get(reactionRef).getString("reaction")
                )

                val targetReaction = if (currentReaction == reaction) IdeaReaction.NONE else reaction
                val nextCount = computeNextUpvoteCount(currentCount, currentReaction, targetReaction)

                transaction.update(
                    ideaRef,
                    mapOf(
                        "upvoteCount" to nextCount,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )

                if (targetReaction == IdeaReaction.NONE) {
                    transaction.delete(reactionRef)
                } else {
                    transaction.set(
                        reactionRef,
                        mapOf(
                            "reaction" to targetReaction.name,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        )
                    )
                }
            }.await()
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    override fun getReaction(ideaId: UUID): Flow<IdeaReaction> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(AppException.UnauthorizedException())
            return@callbackFlow
        }
        trySend(IdeaReaction.NONE)
        val reactionRef = ideas.document(ideaId.toString())
            .collection("reactions")
            .document(uid)

        val registration = reactionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(AppException.NetworkException(error))
                return@addSnapshotListener
            }
            val reaction = reactionFromString(snapshot?.getString("reaction"))
            trySend(reaction)
        }
        awaitClose { registration.remove() }
    }
}

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
private fun reactionFromString(value: String?): IdeaReaction =
    runCatching { value?.let(IdeaReaction::valueOf) }.getOrNull() ?: IdeaReaction.NONE

// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
private fun computeNextUpvoteCount(
    currentCount: Int,
    currentReaction: IdeaReaction,
    targetReaction: IdeaReaction,
): Int = (
    currentCount + when {
        currentReaction == IdeaReaction.UPVOTED && targetReaction != IdeaReaction.UPVOTED -> -1
        currentReaction != IdeaReaction.UPVOTED && targetReaction == IdeaReaction.UPVOTED -> 1
        else -> 0
    }
).coerceAtLeast(0)
