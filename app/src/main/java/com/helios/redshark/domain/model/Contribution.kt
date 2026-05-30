package com.helios.redshark.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ContributionDay(
    val date: LocalDate,
    val count: Int,
    val level: Int,
)

data class ContributionSummary(
    val userId: String,
    val weeks: Int,
    val days: List<ContributionDay>,
    val totalCount: Int,
    val activeDays: Int,
    val maxCount: Int,
)

object ContributionSummaryBuilder {
    fun build(
        userId: String,
        weeks: Int,
        zoneId: ZoneId,
        referenceDate: LocalDate,
        eventInstants: List<Instant>,
    ): ContributionSummary {
        val normalizedWeeks = weeks.coerceAtLeast(1)
        val dayCount = normalizedWeeks * 7
        val startDate = referenceDate.minusDays(dayCount.toLong() - 1)
        val countsByDate = eventInstants
            .map { it.atZone(zoneId).toLocalDate() }
            .filter { !it.isBefore(startDate) && !it.isAfter(referenceDate) }
            .groupingBy { it }
            .eachCount()

        val days = (0 until dayCount).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val count = countsByDate[date] ?: 0
            ContributionDay(
                date = date,
                count = count,
                level = contributionLevel(count),
            )
        }

        return ContributionSummary(
            userId = userId,
            weeks = normalizedWeeks,
            days = days,
            totalCount = days.sumOf { it.count },
            activeDays = days.count { it.count > 0 },
            maxCount = days.maxOfOrNull { it.count } ?: 0,
        )
    }

    private fun contributionLevel(count: Int): Int = when {
        count <= 0 -> 0
        count == 1 -> 1
        count <= 3 -> 2
        count <= 6 -> 3
        else -> 4
    }
}
