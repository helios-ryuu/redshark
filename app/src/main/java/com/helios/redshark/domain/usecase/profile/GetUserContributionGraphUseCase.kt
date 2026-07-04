package com.helios.redshark.domain.usecase.profile

// File nay gom mot hanh dong nghiep vu nho de ViewModel goi ro rang.

import com.helios.redshark.domain.model.ContributionSummary
import com.helios.redshark.domain.repository.ContributionRepository
import java.time.ZoneId
import javax.inject.Inject

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
class GetUserContributionGraphUseCase @Inject constructor(
    private val contributionRepository: ContributionRepository,
// Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
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
