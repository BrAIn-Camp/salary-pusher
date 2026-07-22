package com.braincamp.salarypusher.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Tests for [CoinDenomination] values and display helpers.
 */
class CoinDenominationTest {

    @Test
    fun penny_valueInCents_isOne() {
        assertEquals(1, CoinDenomination.PENNY.valueInCents)
    }

    @Test
    fun nickel_valueInCents_isFive() {
        assertEquals(5, CoinDenomination.NICKEL.valueInCents)
    }

    @Test
    fun dime_valueInCents_isTen() {
        assertEquals(10, CoinDenomination.DIME.valueInCents)
    }

    @Test
    fun quarter_valueInCents_isTwentyFive() {
        assertEquals(25, CoinDenomination.QUARTER.valueInCents)
    }

    @Test
    fun dollar_valueInCents_isOneHundred() {
        assertEquals(100, CoinDenomination.DOLLAR.valueInCents)
    }

    @Test
    fun formatCount_quarter_singular() {
        assertEquals("1 quarter", CoinDenomination.QUARTER.formatCount(1))
    }

    @Test
    fun formatCount_quarter_plural() {
        assertEquals("3 quarters", CoinDenomination.QUARTER.formatCount(3))
    }

    @Test
    fun formatCount_penny_singular() {
        assertEquals("1 penny", CoinDenomination.PENNY.formatCount(1))
    }

    @Test
    fun formatCount_penny_plural() {
        assertEquals("2 pennies", CoinDenomination.PENNY.formatCount(2))
    }

    @Test
    fun formatCount_dollar_singular() {
        assertEquals("1 dollar", CoinDenomination.DOLLAR.formatCount(1))
    }

    @Test
    fun formatCount_dollar_plural() {
        assertEquals("5 dollars", CoinDenomination.DOLLAR.formatCount(5))
    }

    @Test
    fun allDenominations_ordinalIsStable() {
        // Ordinals are persisted — verify they never accidentally change
        assertEquals(0, CoinDenomination.PENNY.ordinal)
        assertEquals(1, CoinDenomination.NICKEL.ordinal)
        assertEquals(2, CoinDenomination.DIME.ordinal)
        assertEquals(3, CoinDenomination.QUARTER.ordinal)
        assertEquals(4, CoinDenomination.DOLLAR.ordinal)
    }
}
