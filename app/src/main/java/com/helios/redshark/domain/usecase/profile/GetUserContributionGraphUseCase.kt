package com.helios.redshark.domain.usecase.profile

import com.helios.redshark.domain.model.ContributionSummary
import com.helios.redshark.domain.repository.ContributionRepository
import java.time.ZoneId
import javax.inject.Inject

class GetUserContributionGraphUseCase @Inject constructor(
    private val contributionRepository: ContributionRepository,
) {
    suspend operator fun invoke(
        userId: String,
        weeks: Int = DEFAULT_WEEKS,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): ContributionSummary =
        contributionRepository.getUserContributionSummary(userId, weeks, zoneId)

    private companion object {
        const val DEFAULT_WEEKS = 12
    }
}
