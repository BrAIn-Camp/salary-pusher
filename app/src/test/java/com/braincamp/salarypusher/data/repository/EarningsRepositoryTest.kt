package com.braincamp.salarypusher.data.repository

import com.braincamp.salarypusher.data.db.EarningEventDao
import com.braincamp.salarypusher.data.db.EarningEventEntity
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.domain.model.EarningsSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for [EarningsRepository].
 *
 * Uses MockK to stub the DAO — no Android context required.
 * Timestamp boundary logic is tested against known dates.
 */
class EarningsRepositoryTest {

    private lateinit var dao: EarningEventDao
    private lateinit var repository: EarningsRepository

    @Before
    fun setUp() {
        dao = mockk()
        repository = EarningsRepository(dao)
    }

    // ── recordEarning ─────────────────────────────────────────────────────────

    @Test
    fun recordEarning_quarter_insertsCorrectCentAmount() = runTest {
        val insertedSlot = slot<EarningEventEntity>()
        coEvery { dao.insert(capture(insertedSlot)) } returns 1L

        repository.recordEarning(CoinDenomination.QUARTER)

        assertEquals(25L, insertedSlot.captured.amountCents)
        assertEquals(CoinDenomination.QUARTER.ordinal, insertedSlot.captured.denominationOrdinal)
    }

    @Test
    fun recordEarning_dollar_insertsCorrectCentAmount() = runTest {
        val insertedSlot = slot<EarningEventEntity>()
        coEvery { dao.insert(capture(insertedSlot)) } returns 1L

        repository.recordEarning(CoinDenomination.DOLLAR)

        assertEquals(100L, insertedSlot.captured.amountCents)
    }

    @Test
    fun recordEarning_penny_insertsCorrectCentAmount() = runTest {
        val insertedSlot = slot<EarningEventEntity>()
        coEvery { dao.insert(capture(insertedSlot)) } returns 1L

        repository.recordEarning(CoinDenomination.PENNY)

        assertEquals(1L, insertedSlot.captured.amountCents)
    }

    // ── getEarningsSummary ────────────────────────────────────────────────────

    @Test
    fun getEarningsSummary_emptyTable_returnsAllZeros() = runTest {
        every { dao.getEventsSince(0L) } returns flowOf(emptyList())

        val summary = repository.getEarningsSummary().first()

        assertEquals(EarningsSummary.EMPTY, summary)
    }

    @Test
    fun getEarningsSummary_onlyTodayEvents_correctTodayTotal() = runTest {
        val todayTs = repository.todayStartMillis() + 3_600_000L // 1 hour after midnight today
        val events = listOf(
            EarningEventEntity(amountCents = 25L, timestamp = todayTs, denominationOrdinal = 3),
            EarningEventEntity(amountCents = 25L, timestamp = todayTs + 1000, denominationOrdinal = 3)
        )
        every { dao.getEventsSince(0L) } returns flowOf(events)

        val summary = repository.getEarningsSummary().first()

        assertEquals(50L, summary.todayTotalCents)
        assertEquals(50L, summary.weekTotalCents)
        assertEquals(50L, summary.allTimeTotalCents)
    }

    @Test
    fun getEarningsSummary_oldEventNotCountedToday() = runTest {
        val yesterday = repository.todayStartMillis() - 1000L // 1 second before midnight
        val todayTs = repository.todayStartMillis() + 3_600_000L

        val events = listOf(
            EarningEventEntity(amountCents = 100L, timestamp = yesterday, denominationOrdinal = 4), // yesterday
            EarningEventEntity(amountCents = 25L,  timestamp = todayTs,   denominationOrdinal = 3)  // today
        )
        every { dao.getEventsSince(0L) } returns flowOf(events)

        val summary = repository.getEarningsSummary().first()

        assertEquals(25L,  summary.todayTotalCents)
        assertEquals(125L, summary.allTimeTotalCents)
    }

    // ── todayStartMillis ──────────────────────────────────────────────────────

    @Test
    fun todayStartMillis_isAtMidnight() {
        val millis = repository.todayStartMillis()
        val localDate = java.time.Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        assertEquals(0, localDate.hour)
        assertEquals(0, localDate.minute)
        assertEquals(0, localDate.second)
    }

    // ── weekStartMillis ───────────────────────────────────────────────────────

    @Test
    fun weekStartMillis_isMonday() {
        val millis = repository.weekStartMillis()
        val dayOfWeek = java.time.Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .dayOfWeek
        assertEquals(java.time.DayOfWeek.MONDAY, dayOfWeek)
    }

    @Test
    fun weekStartMillis_isAtMidnight() {
        val millis = repository.weekStartMillis()
        val localDateTime = java.time.Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        assertEquals(0, localDateTime.hour)
        assertEquals(0, localDateTime.minute)
    }
}
