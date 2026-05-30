package com.helios.redshark.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.helios.redshark.core.error.AppException
import com.helios.redshark.domain.model.ContributionSummary
import com.helios.redshark.domain.model.ContributionSummaryBuilder
import com.helios.redshark.domain.repository.ContributionRepository
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContributionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ContributionRepository {

    override suspend fun getUserContributionSummary(
        userId: String,
        weeks: Int,
        zoneId: ZoneId,
    ): ContributionSummary {
        val normalizedWeeks = weeks.coerceAtLeast(1)
        val referenceDate = LocalDate.now(zoneId)
        val startDate = referenceDate.minusDays((normalizedWeeks * 7).toLong() - 1)
        val startTimestamp = Timestamp(Date.from(startDate.atStartOfDay(zoneId).toInstant()))

        return try {
            val eventInstants = queryCreatedAt("ideas", userId, startTimestamp, hasSoftDelete = true) +
                queryCreatedAt("issues", userId, startTimestamp, hasSoftDelete = true) +
                queryCreatedAt("comments", userId, startTimestamp, hasSoftDelete = false)

            ContributionSummaryBuilder.build(
                userId = userId,
                weeks = normalizedWeeks,
                zoneId = zoneId,
                referenceDate = referenceDate,
                eventInstants = eventInstants,
            )
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.NetworkException(e)
        }
    }

    private suspend fun queryCreatedAt(
        collectionName: String,
        userId: String,
        startTimestamp: Timestamp,
        hasSoftDelete: Boolean,
    ) = firestore.collection(collectionName)
        .whereEqualTo("authorId", userId)
        .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get()
        .await()
        .documents
        .asSequence()
        .filter { doc -> !hasSoftDelete || doc.getTimestamp("deletedAt") == null }
        .mapNotNull { doc -> doc.getTimestamp("createdAt")?.toDate()?.toInstant() }
        .toList()
}
