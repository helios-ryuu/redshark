package com.helios.redshark.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ContributionSummaryBuilderTest {

    private val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    private val referenceDate = LocalDate.of(2026, 5, 30)

    @Test
    fun `build creates twelve week window and ignores events outside range`() {
        val inRangeDate = LocalDate.of(2026, 5, 30)
        val outsideDate = referenceDate.minusDays(84)

        val summary = ContributionSummaryBuilder.build(
            userId = "user_1",
            weeks = 12,
            zoneId = zoneId,
            referenceDate = referenceDate,
            eventInstants = listOf(
                instantAt(inRangeDate),
                instantAt(inRangeDate),
                instantAt(outsideDate),
            ),
        )

        assertEquals(84, summary.days.size)
        assertEquals(2, summary.totalCount)
        assertEquals(1, summary.activeDays)
        assertEquals(2, summary.maxCount)
        assertEquals(2, summary.days.first { it.date == inRangeDate }.count)
        assertEquals(0, summary.days.count { it.date == outsideDate })
    }

    @Test
    fun `build assigns stable intensity levels`() {
        val one = referenceDate.minusDays(2)
        val three = referenceDate.minusDays(1)
        val seven = referenceDate

        val summary = ContributionSummaryBuilder.build(
            userId = "user_1",
            weeks = 12,
            zoneId = zoneId,
            referenceDate = referenceDate,
            eventInstants = listOf(
                instantAt(one),
                instantAt(three),
                instantAt(three),
                instantAt(three),
            ) + List(7) { instantAt(seven) },
        )

        assertEquals(1, summary.days.first { it.date == one }.level)
        assertEquals(2, summary.days.first { it.date == three }.level)
        assertEquals(4, summary.days.first { it.date == seven }.level)
    }

    private fun instantAt(date: LocalDate) =
        date.atTime(LocalTime.NOON).atZone(zoneId).toInstant()
}
