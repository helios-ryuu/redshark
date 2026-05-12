package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.CreateIssueInput
import com.helios.redshark.domain.model.Issue
import com.helios.redshark.domain.model.IssueStatus
import com.helios.redshark.domain.model.UpdateIssueInput
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IssueRepository {
    /** Live list of issues attached to a specific idea (deletedAt IS NULL). */
    fun getIssuesByIdea(ideaId: UUID): Flow<List<Issue>>

    /** Live feed of OPEN issues created by other users (home screen). */
    fun getOpenIssuesFromOthers(): Flow<List<Issue>>

    suspend fun getIssueDetail(id: UUID): Issue

    /**
     * Returns the count of active issues (OPEN | IN_PROGRESS, deletedAt IS NULL)
     * owned by the current authenticated user.
     * Used to enforce the 20-issue limit before creating a new issue.
     */
    suspend fun countMyActiveIssues(): Int

    /** Creates an issue with status=OPEN. Limit check happens in the use-case layer. */
    suspend fun create(input: CreateIssueInput): Issue

    /** Updates editable fields (title, description, priority, assignee). TC-C11 */
    suspend fun update(id: UUID, input: UpdateIssueInput): Issue

    /** Persists a status change that has already been validated by the state machine. */
    suspend fun updateStatus(id: UUID, newStatus: IssueStatus): Issue

    /** Sets deletedAt on the server; the record is NOT physically removed. */
    suspend fun softDelete(id: UUID)
}
