package com.helios.redshark.domain.repository

// File nay dinh nghia hop dong du lieu ma tang domain can su dung.

import com.helios.redshark.domain.model.ContributionSummary
import java.time.ZoneId

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
interface ContributionRepository {
    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    suspend fun getUserContributionSummary(
        userId: String,
        weeks: Int,
        zoneId: ZoneId,
    ): ContributionSummary
}
