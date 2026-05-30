package com.helios.redshark.domain.repository

import com.helios.redshark.domain.model.ContributionSummary
import java.time.ZoneId

interface ContributionRepository {
    suspend fun getUserContributionSummary(
        userId: String,
        weeks: Int,
        zoneId: ZoneId,
    ): ContributionSummary
}
