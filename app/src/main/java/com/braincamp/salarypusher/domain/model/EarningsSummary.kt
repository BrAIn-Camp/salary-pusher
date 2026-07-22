package com.braincamp.salarypusher.domain.model

/**
 * Aggregated earnings totals across three time windows.
 *
 * All amounts are in cents to avoid floating point errors.
 * Format for display using [formatCents] from the util package.
 *
 * [todayTotalCents]    — earnings since midnight today (local time)
 * [weekTotalCents]     — earnings since Monday midnight (local time)
 * [allTimeTotalCents]  — all earnings ever recorded
 */
data class EarningsSummary(
    val todayTotalCents: Long,
    val weekTotalCents: Long,
    val allTimeTotalCents: Long
) {
    companion object {
        /** Convenience empty state — used as initial value before DB loads. */
        val EMPTY = EarningsSummary(
            todayTotalCents = 0L,
            weekTotalCents = 0L,
            allTimeTotalCents = 0L
        )
    }
}
