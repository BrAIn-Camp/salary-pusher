package com.braincamp.salarypusher.game.engine

import com.braincamp.salarypusher.domain.model.WorkProfile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The result of an accrual calculation.
 *
 * [coins]          — whole coins the player has earned and can drop.
 * [remainderCents] — cents that did not reach a full coin denomination.
 *                    Must be carried forward into the next calculation to
 *                    prevent earnings loss from rounding.
 */
data class AccrualResult(
    val coins: Int,
    val remainderCents: Long
)

/**
 * Calculates how many coins of a given denomination have accrued between
 * two timestamps, given a [WorkProfile].
 *
 * Rules:
 * - Only time that falls within defined work hours is counted.
 * - Shift boundaries are respected to the second.
 * - A [carryInCents] remainder from the previous tick is folded in so
 *   no sub-denomination earnings are ever lost.
 * - The calculation is deterministic — the same inputs always produce
 *   the same output regardless of when it is called.
 *
 * All timestamps are epoch milliseconds (UTC).
 */
class CoinAccrualCalculator {

    /**
     * Calculates accrued coins between [fromTimestamp] and [toTimestamp].
     *
     * @param fromTimestamp  Start of the window (epoch millis, inclusive).
     * @param toTimestamp    End of the window (epoch millis, exclusive).
     * @param workProfile    The player's salary and schedule.
     * @param carryInCents   Remainder from the previous tick (default 0).
     * @return               [AccrualResult] with whole coins and new remainder.
     */
    fun calculate(
        fromTimestamp: Long,
        toTimestamp: Long,
        workProfile: WorkProfile,
        carryInCents: Long = 0L
    ): AccrualResult {
        if (toTimestamp <= fromTimestamp) return AccrualResult(0, carryInCents)

        val zone = ZoneId.systemDefault()
        val from = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(fromTimestamp), zone
        )
        val to = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(toTimestamp), zone
        )

        val workSeconds = calculateWorkSeconds(from, to, workProfile)
        val earnedCents = carryInCents + centsEarned(workSeconds, workProfile.hourlySalaryCents)
        val denominationCents = workProfile.coinDenomination.valueInCents.toLong()

        val coins = (earnedCents / denominationCents).toInt()
        val remainder = earnedCents % denominationCents

        return AccrualResult(coins = coins, remainderCents = remainder)
    }

    /**
     * Returns total seconds of work time within the window [from, to].
     *
     * Iterates over each calendar day spanned by the window and sums
     * the overlap between that day's shift hours and the window.
     */
    private fun calculateWorkSeconds(
        from: LocalDateTime,
        to: LocalDateTime,
        profile: WorkProfile
    ): Long {
        var totalSeconds = 0L

        // Walk each day from the start date to the end date (inclusive)
        var currentDate = from.toLocalDate()
        val endDate = to.toLocalDate()

        while (!currentDate.isAfter(endDate)) {
            // Only process work days
            if (currentDate.dayOfWeek in profile.workDays) {
                val shiftStart = currentDate.atTime(profile.shiftStart)
                val shiftEnd   = currentDate.atTime(profile.shiftEnd)

                // Effective window = intersection of [from, to] and [shiftStart, shiftEnd]
                val effectiveStart = maxOf(from, shiftStart)
                val effectiveEnd   = minOf(to, shiftEnd)

                if (effectiveEnd.isAfter(effectiveStart)) {
                    totalSeconds += java.time.Duration.between(effectiveStart, effectiveEnd).seconds
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        return totalSeconds
    }

    /**
     * Converts a duration of work seconds into cents earned.
     *
     * Formula: (seconds / 3600) * hourlySalaryCents
     * Uses Long arithmetic throughout to avoid floating point errors.
     */
    private fun centsEarned(workSeconds: Long, hourlySalaryCents: Long): Long {
        // Multiply first to preserve precision before dividing
        return (workSeconds * hourlySalaryCents) / 3600L
    }
}
