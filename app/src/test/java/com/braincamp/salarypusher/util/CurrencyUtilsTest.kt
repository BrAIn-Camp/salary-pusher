package com.braincamp.salarypusher.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun formatCents_zero_returnsZeroDollars() {
        assertEquals("$0.00", formatCents(0))
    }

    @Test
    fun formatCents_oneCent_returnsCorrect() {
        assertEquals("$0.01", formatCents(1))
    }

    @Test
    fun formatCents_quarter_returnsCorrect() {
        assertEquals("$0.25", formatCents(25))
    }

    @Test
    fun formatCents_oneDollar_returnsCorrect() {
        assertEquals("$1.00", formatCents(100))
    }

    @Test
    fun formatCents_largeAmount_formatsWithCommas() {
        assertEquals("$1,234.56", formatCents(123456))
    }

    @Test
    fun annualCentsToHourlyCents_104000Dollars_returns50Dollars() {
        val annualCents = 104_000L * 100  // $104,000.00
        val expected = 50L * 100           // $50.00/hr
        assertEquals(expected, annualCentsToHourlyCents(annualCents))
    }

    @Test
    fun annualCentsToHourlyCents_41600Dollars_returns20Dollars() {
        val annualCents = 41_600L * 100   // $41,600.00
        val expected = 20L * 100           // $20.00/hr
        assertEquals(expected, annualCentsToHourlyCents(annualCents))
    }

    @Test
    fun hourlyCentsToAnnualCents_50DollarsHour_returns104000Dollars() {
        val hourlyCents = 50L * 100        // $50.00/hr
        val expected = 104_000L * 100      // $104,000.00
        assertEquals(expected, hourlyCentsToAnnualCents(hourlyCents))
    }

    @Test
    fun annualToHourly_roundTrip_isConsistent() {
        val originalHourly = 7500L // $75.00/hr
        val annual = hourlyCentsToAnnualCents(originalHourly)
        val backToHourly = annualCentsToHourlyCents(annual)
        assertEquals(originalHourly, backToHourly)
    }
}
