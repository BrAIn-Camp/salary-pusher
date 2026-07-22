package com.braincamp.salarypusher.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * The player's complete work configuration.
 *
 * Always stored with salary as an hourly rate in cents (avoids floating point errors).
 * Annual salaries are converted to hourly before storage: annualCents / 2080.
 */
data class WorkProfile(
    val hourlySalaryCents: Long,
    val workDays: Set<DayOfWeek>,
    val shiftStartHour: Int,    // 0-23
    val shiftStartMinute: Int,  // 0-59
    val shiftEndHour: Int,      // 0-23
    val shiftEndMinute: Int,    // 0-59
    val coinDenomination: CoinDenomination
) {
    /**
     * Returns true if the given [dateTime] falls within this profile's work hours.
     *
     * A moment is "in shift" when:
     * - Its day of week is in [workDays], AND
     * - Its time is >= shift start AND < shift end
     *
     * The shift end boundary is exclusive so a coin is not awarded for the exact
     * end-of-shift instant.
     */
    fun isWorkHour(dateTime: LocalDateTime): Boolean {
        if (dateTime.dayOfWeek !in workDays) return false
        val time = dateTime.toLocalTime()
        val start = LocalTime.of(shiftStartHour, shiftStartMinute)
        val end = LocalTime.of(shiftEndHour, shiftEndMinute)
        return time >= start && time < end
    }

    /**
     * Returns the shift start time as a [LocalTime].
     */
    val shiftStart: LocalTime get() = LocalTime.of(shiftStartHour, shiftStartMinute)

    /**
     * Returns the shift end time as a [LocalTime].
     */
    val shiftEnd: LocalTime get() = LocalTime.of(shiftEndHour, shiftEndMinute)
}
