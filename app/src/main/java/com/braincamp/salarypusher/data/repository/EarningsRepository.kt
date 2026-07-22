package com.braincamp.salarypusher.data.repository

import com.braincamp.salarypusher.data.db.EarningEventDao
import com.braincamp.salarypusher.data.db.EarningEventEntity
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.domain.model.EarningsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Repository for all earning event operations.
 *
 * Responsible for recording coins collected from the front edge and
 * computing aggregated earnings summaries.
 *
 * All timestamp boundaries are calculated in the device's local timezone.
 */
class EarningsRepository(private val dao: EarningEventDao) {

    /**
     * Records a single coin collected from the front edge.
     *
     * [denomination] determines the amount — this is the only valid way
     * to create an earning event. Never pass a custom amountCents.
     */
    suspend fun recordEarning(denomination: CoinDenomination) {
        dao.insert(
            EarningEventEntity(
                amountCents = denomination.valueInCents.toLong(),
                timestamp = System.currentTimeMillis(),
                denominationOrdinal = denomination.ordinal
            )
        )
    }

    /**
     * Returns a live [Flow] of [EarningsSummary] that updates whenever
     * a new earning is recorded.
     *
     * Uses the device's local timezone to determine "today" and "this week".
     */
    fun getEarningsSummary(): Flow<EarningsSummary> = flow {
        // Re-emit whenever the underlying data changes
        dao.getEventsSince(0L).collect { allEvents ->
            val now = System.currentTimeMillis()
            val todayStart = todayStartMillis()
            val weekStart = weekStartMillis()

            val todayTotal = allEvents
                .filter { it.timestamp >= todayStart }
                .sumOf { it.amountCents }

            val weekTotal = allEvents
                .filter { it.timestamp >= weekStart }
                .sumOf { it.amountCents }

            val allTimeTotal = allEvents.sumOf { it.amountCents }

            emit(EarningsSummary(
                todayTotalCents = todayTotal,
                weekTotalCents = weekTotal,
                allTimeTotalCents = allTimeTotal
            ))
        }
    }

    // ── Timestamp boundary helpers ────────────────────────────────────────────

    /**
     * Returns the epoch millis for midnight at the start of today (local time).
     */
    fun todayStartMillis(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Returns the epoch millis for midnight at the start of the most recent Monday (local time).
     */
    fun weekStartMillis(): Long {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        // If today is before Monday (shouldn't happen with DayOfWeek.MONDAY), go back a week
        val adjustedMonday = if (monday.isAfter(today)) monday.minusWeeks(1) else monday
        return adjustedMonday
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
