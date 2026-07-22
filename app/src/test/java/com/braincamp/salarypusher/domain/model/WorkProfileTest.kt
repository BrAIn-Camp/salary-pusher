package com.braincamp.salarypusher.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Tests for [WorkProfile.isWorkHour].
 *
 * These are the most critical boundary condition tests in the project.
 * The salary clock depends entirely on this logic being correct.
 *
 * Test profile: Mon-Fri, 9:00 AM - 5:00 PM
 */
class WorkProfileTest {

    private val profile = WorkProfile(
        hourlySalaryCents = 5000L, // $50.00/hr
        workDays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        ),
        shiftStartHour = 9,
        shiftStartMinute = 0,
        shiftEndHour = 17,
        shiftEndMinute = 0,
        coinDenomination = CoinDenomination.QUARTER
    )

    // ── During shift ──────────────────────────────────────────────────────────

    @Test
    fun isWorkHour_midShiftMonday_returnsTrue() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 12, 0) // Monday noon
        assertTrue(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_midShiftFriday_returnsTrue() {
        val dateTime = LocalDateTime.of(2024, 1, 12, 14, 30) // Friday 2:30 PM
        assertTrue(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_oneMinuteAfterShiftStart_returnsTrue() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 9, 1) // Monday 9:01 AM
        assertTrue(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_oneMinuteBeforeShiftEnd_returnsTrue() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 16, 59) // Monday 4:59 PM
        assertTrue(profile.isWorkHour(dateTime))
    }

    // ── Exactly at boundaries ─────────────────────────────────────────────────

    @Test
    fun isWorkHour_exactlyAtShiftStart_returnsTrue() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 9, 0) // Monday 9:00 AM exactly
        assertTrue("Shift start boundary should be inclusive", profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_exactlyAtShiftEnd_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 17, 0) // Monday 5:00 PM exactly
        assertFalse("Shift end boundary should be exclusive", profile.isWorkHour(dateTime))
    }

    // ── Before and after shift ────────────────────────────────────────────────

    @Test
    fun isWorkHour_beforeShiftStart_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 8, 59) // Monday 8:59 AM
        assertFalse(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_afterShiftEnd_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 17, 1) // Monday 5:01 PM
        assertFalse(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_midnight_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 8, 0, 0) // Monday midnight
        assertFalse(profile.isWorkHour(dateTime))
    }

    // ── Non-work days ─────────────────────────────────────────────────────────

    @Test
    fun isWorkHour_saturdayDuringNormalHours_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 13, 12, 0) // Saturday noon
        assertFalse(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_sundayDuringNormalHours_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 14, 10, 0) // Sunday 10 AM
        assertFalse(profile.isWorkHour(dateTime))
    }

    @Test
    fun isWorkHour_saturdayAtExactShiftStartTime_returnsFalse() {
        val dateTime = LocalDateTime.of(2024, 1, 13, 9, 0) // Saturday 9:00 AM
        assertFalse("Non-work day should always return false regardless of time", profile.isWorkHour(dateTime))
    }

    // ── Custom schedule edge cases ────────────────────────────────────────────

    @Test
    fun isWorkHour_singleWorkDay_correctDayReturnsTrue() {
        val wednesdayOnly = profile.copy(workDays = setOf(DayOfWeek.WEDNESDAY))
        val wednesday = LocalDateTime.of(2024, 1, 10, 12, 0) // Wednesday noon
        assertTrue(wednesdayOnly.isWorkHour(wednesday))
    }

    @Test
    fun isWorkHour_singleWorkDay_otherDayReturnsFalse() {
        val wednesdayOnly = profile.copy(workDays = setOf(DayOfWeek.WEDNESDAY))
        val tuesday = LocalDateTime.of(2024, 1, 9, 12, 0) // Tuesday noon
        assertFalse(wednesdayOnly.isWorkHour(tuesday))
    }

    @Test
    fun isWorkHour_nonZeroStartMinute_respectsMinutes() {
        val profile30MinStart = profile.copy(shiftStartHour = 9, shiftStartMinute = 30)
        val before = LocalDateTime.of(2024, 1, 8, 9, 29) // 9:29 AM
        val after = LocalDateTime.of(2024, 1, 8, 9, 30)  // 9:30 AM
        assertFalse(profile30MinStart.isWorkHour(before))
        assertTrue(profile30MinStart.isWorkHour(after))
    }
}
