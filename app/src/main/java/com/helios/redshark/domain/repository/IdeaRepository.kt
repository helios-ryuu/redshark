package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.CreateIdeaInput
import com.helios.redshark.domain.model.Idea
import com.helios.redshark.domain.model.IdeaStatus
import com.helios.redshark.domain.model.UpdateIdeaInput
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IdeaRepository {
    /** Emits a live list of ideas authored by the current user (deletedAt IS NULL). */
    fun getMyIdeas(): Flow<List<Idea>>

    suspend fun getIdeaDetail(id: UUID): Idea

    /** Inserts a new idea with status=ACTIVE. Validation occurs in the use-case layer. */
    suspend fun create(input: CreateIdeaInput): Idea

    suspend fun update(id: UUID, input: UpdateIdeaInput): Idea

    /** TC-C08: changes status (ACTIVE → CLOSED / CANCELLED). */
    suspend fun updateStatus(id: UUID, newStatus: IdeaStatus): Idea

    /** Sets deletedAt on the server; the record is NOT physically removed. */
    suspend fun softDelete(id: UUID)

    /** Adds a collaborator id using set semantics (no duplicate entries). */
    suspend fun addCollaborator(id: UUID, collaboratorId: String): Idea
}
