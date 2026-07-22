package com.braincamp.salarypusher.game.engine

import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.domain.model.WorkProfile
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Tests for [CoinAccrualCalculator].
 *
 * These are the most critical tests in the project. The salary simulation
 * depends entirely on this math being correct.
 *
 * All test timestamps use a fixed weekday (Monday 2024-01-08) to avoid
 * weekend/holiday edge cases unless specifically testing those boundaries.
 *
 * Helper: [ts] converts a [LocalDateTime] to epoch millis in the system zone.
 */
class CoinAccrualCalculatorTest {

    private lateinit var calculator: CoinAccrualCalculator

    // Standard Mon-Fri 9-5 profile at $40/hr using quarters
    // $40/hr = 4000 cents/hr
    // 1 quarter every 4000/25 = 160 seconds (= 2 min 40 sec)
    // In 1 hour: 4000 / 25 = 160 quarters
    private val quarterProfile = WorkProfile(
        hourlySalaryCents = 4000L,  // $40.00/hr
        workDays = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        ),
        shiftStartHour = 9,
        shiftStartMinute = 0,
        shiftEndHour = 17,
        shiftEndMinute = 0,
        coinDenomination = CoinDenomination.QUARTER
    )

    // $60/hr profile with dollar coins
    // $60/hr = 6000 cents/hr
    // In 30 min: 3000 cents / 100 = 30 dollar coins
    private val dollarProfile = quarterProfile.copy(
        hourlySalaryCents = 6000L,
        coinDenomination = CoinDenomination.DOLLAR
    )

    // $20/hr profile with pennies
    // $20/hr = 2000 cents/hr
    // In 1 hour: 2000 pennies
    private val pennyProfile = quarterProfile.copy(
        hourlySalaryCents = 2000L,
        coinDenomination = CoinDenomination.PENNY
    )

    @Before
    fun setUp() {
        calculator = CoinAccrualCalculator()
    }

    // ─── Core accrual math ────────────────────────────────────────────────────

    @Test
    fun calculate_oneHourAtFortyDollarsWithQuarters_returns160Coins() {
        val from = ts(2024, 1, 8, 10, 0)   // Monday 10:00 AM
        val to   = ts(2024, 1, 8, 11, 0)   // Monday 11:00 AM (1 hour)

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(160, result.coins)
        assertEquals(0L, result.remainderCents)
    }

    @Test
    fun calculate_thirtyMinutesAtSixtyDollarsWithDollars_returns30Coins() {
        val from = ts(2024, 1, 8, 10, 0)   // Monday 10:00 AM
        val to   = ts(2024, 1, 8, 10, 30)  // Monday 10:30 AM (30 min)

        val result = calculator.calculate(from, to, dollarProfile)

        assertEquals(30, result.coins)
        assertEquals(0L, result.remainderCents)
    }

    @Test
    fun calculate_oneHourAtTwentyDollarsWithPennies_returns2000Coins() {
        val from = ts(2024, 1, 8, 10, 0)
        val to   = ts(2024, 1, 8, 11, 0)

        val result = calculator.calculate(from, to, pennyProfile)

        assertEquals(2000, result.coins)
        assertEquals(0L, result.remainderCents)
    }

    // ─── Shift boundary handling ──────────────────────────────────────────────

    @Test
    fun calculate_windowEntirelyOutsideShift_returnsZeroCoins() {
        val from = ts(2024, 1, 8, 7, 0)    // Monday 7:00 AM (before shift)
        val to   = ts(2024, 1, 8, 8, 59)   // Monday 8:59 AM (still before shift)

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(0, result.coins)
    }

    @Test
    fun calculate_windowStartsBeforeShiftEndsAfter_countsOnlyShiftTime() {
        // Window: 8:00 AM - 10:00 AM. Shift: 9:00 AM - 5:00 PM.
        // Overlap: 9:00 AM - 10:00 AM = 1 hour = 160 quarters
        val from = ts(2024, 1, 8, 8, 0)
        val to   = ts(2024, 1, 8, 10, 0)

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(160, result.coins)
    }

    @Test
    fun calculate_windowStartsDuringShiftEndsAfter_countsOnlyShiftTime() {
        // Window: 4:00 PM - 6:00 PM. Shift ends at 5:00 PM.
        // Overlap: 4:00 PM - 5:00 PM = 1 hour = 160 quarters
        val from = ts(2024, 1, 8, 16, 0)
        val to   = ts(2024, 1, 8, 18, 0)

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(160, result.coins)
    }

    @Test
    fun calculate_windowExactlyAtShiftBoundaries_countsFullShift() {
        // Window = exactly the 8-hour shift
        val from = ts(2024, 1, 8, 9, 0)
        val to   = ts(2024, 1, 8, 17, 0)

        val result = calculator.calculate(from, to, quarterProfile)

        // 8 hours * 160 quarters/hr = 1280 quarters
        assertEquals(1280, result.coins)
    }

    // ─── Non-work day handling ────────────────────────────────────────────────

    @Test
    fun calculate_onSaturday_returnsZeroCoins() {
        val from = ts(2024, 1, 13, 10, 0)  // Saturday 10:00 AM
        val to   = ts(2024, 1, 13, 11, 0)  // Saturday 11:00 AM

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(0, result.coins)
    }

    @Test
    fun calculate_onSunday_returnsZeroCoins() {
        val from = ts(2024, 1, 14, 10, 0)  // Sunday 10:00 AM
        val to   = ts(2024, 1, 14, 11, 0)

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(0, result.coins)
    }

    // ─── Multi-day window spanning shift boundary ─────────────────────────────

    @Test
    fun calculate_twoHourWindowSpanningMidnight_countsBothDaysShiftTime() {
        // Window: Monday 4:30 PM -> Tuesday 10:30 AM
        // Monday overlap: 4:30 PM - 5:00 PM = 30 min = 80 quarters
        // Tuesday overlap: 9:00 AM - 10:30 AM = 90 min = 240 quarters
        // Total: 320 quarters
        val from = ts(2024, 1, 8, 16, 30)   // Monday 4:30 PM
        val to   = ts(2024, 1, 9, 10, 30)   // Tuesday 10:30 AM

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(320, result.coins)
    }

    @Test
    fun calculate_windowSpanningWeekend_onlyCountsWeekdayShiftTime() {
        // Window: Friday 4:00 PM -> Monday 10:00 AM
        // Friday overlap: 4:00 PM - 5:00 PM = 1 hr = 160 quarters
        // Saturday: 0 (not a work day)
        // Sunday: 0 (not a work day)
        // Monday overlap: 9:00 AM - 10:00 AM = 1 hr = 160 quarters
        // Total: 320 quarters
        val from = ts(2024, 1, 12, 16, 0)   // Friday 4:00 PM
        val to   = ts(2024, 1, 15, 10, 0)   // Monday 10:00 AM

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(320, result.coins)
    }

    // ─── Remainder / carry-in handling ────────────────────────────────────────

    @Test
    fun calculate_fractionalCentsProduceRemainder() {
        // 1 second at $40/hr = 4000/3600 cents = 1.11... cents
        // With quarters: 1.11 cents < 25 cents, so 0 coins, remainder = 1 cent
        val from = ts(2024, 1, 8, 10, 0, 0)
        val to   = ts(2024, 1, 8, 10, 0, 1) // exactly 1 second later

        val result = calculator.calculate(from, to, quarterProfile)

        assertEquals(0, result.coins)
        assertEquals(1L, result.remainderCents) // floor(4000/3600) = 1 cent
    }

    @Test
    fun calculate_carryInPushesOverDenominationThreshold() {
        // 1 second earns 1 cent. Carry in 24 cents. Total = 25 cents = 1 quarter.
        val from = ts(2024, 1, 8, 10, 0, 0)
        val to   = ts(2024, 1, 8, 10, 0, 1)

        val result = calculator.calculate(from, to, quarterProfile, carryInCents = 24L)

        assertEquals(1, result.coins)
        assertEquals(0L, result.remainderCents)
    }

    @Test
    fun calculate_twoConsecutiveWindowsMatchOneCombimedWindow() {
        // Splitting a window in half and summing should equal the full window result.
        // This verifies no earnings are lost at the seam.
        val start  = ts(2024, 1, 8, 10, 0)
        val middle = ts(2024, 1, 8, 11, 0)
        val end    = ts(2024, 1, 8, 12, 0)

        val firstHalf  = calculator.calculate(start, middle, quarterProfile)
        val secondHalf = calculator.calculate(middle, end, quarterProfile, firstHalf.remainderCents)
        val combined   = calculator.calculate(start, end, quarterProfile)

        assertEquals(combined.coins, firstHalf.coins + secondHalf.coins)
        assertEquals(combined.remainderCents, secondHalf.remainderCents)
    }

    @Test
    fun calculate_emptyWindow_returnsCarryInUnchanged() {
        val ts = ts(2024, 1, 8, 10, 0)

        val result = calculator.calculate(ts, ts, quarterProfile, carryInCents = 17L)

        assertEquals(0, result.coins)
        assertEquals(17L, result.remainderCents)
    }

    @Test
    fun calculate_reversedWindow_returnsCarryInUnchanged() {
        val from = ts(2024, 1, 8, 11, 0)
        val to   = ts(2024, 1, 8, 10, 0)

        val result = calculator.calculate(from, to, quarterProfile, carryInCents = 5L)

        assertEquals(0, result.coins)
        assertEquals(5L, result.remainderCents)
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private fun ts(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): Long {
        return LocalDateTime.of(year, month, day, hour, minute, second)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
