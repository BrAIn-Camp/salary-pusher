package com.braincamp.salarypusher.game.engine

import com.braincamp.salarypusher.data.datastore.UserPreferencesRepository
import com.braincamp.salarypusher.domain.model.WorkProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/**
 * How often the clock ticks while the app is in the foreground (milliseconds).
 * One tick per minute is fine-grained enough to feel live without draining battery.
 */
private const val TICK_INTERVAL_MS = 60_000L

/**
 * Manages the salary clock — the engine that converts real elapsed work time
 * into coins for the drop queue.
 *
 * Responsibilities:
 * 1. On app open: calculate coins accrued since the last recorded tick
 *    (handles offline gaps, even overnight or over a weekend).
 * 2. While foregrounded during work hours: emit coins on every tick interval.
 * 3. Persist the last tick time so gaps can be calculated correctly.
 *
 * Consumers collect [coinAccrualFlow] and add each emitted [AccrualResult]
 * to the drop queue via [DropQueueViewModel].
 *
 * @param calculator            Pure calculation engine — no side effects.
 * @param preferencesRepository Source of [WorkProfile] and last tick timestamp.
 */
class SalaryClock(
    private val calculator: CoinAccrualCalculator,
    private val preferencesRepository: UserPreferencesRepository
) {

    /**
     * Emits an [AccrualResult] on startup (for the offline gap) and then
     * once per [TICK_INTERVAL_MS] during work hours while the app is open.
     *
     * Emits a zero-coin result outside work hours so the queue is never
     * incremented incorrectly — consumers can ignore zero-coin results.
     *
     * Carries the remainder between ticks internally so no sub-denomination
     * earnings are lost across tick boundaries.
     */
    val coinAccrualFlow: Flow<AccrualResult> = flow {
        var carryInCents = 0L

        // Combine the latest work profile and last tick timestamp
        combine(
            preferencesRepository.workProfileFlow,
            preferencesRepository.lastTickTimestamp
        ) { profile, lastTick -> Pair(profile, lastTick) }
            .distinctUntilChanged()
            .collect { (profile, lastTickTimestamp) ->

                // ── Startup gap calculation ──────────────────────────────────
                // Calculate everything accrued between the last recorded tick and now.
                val now = System.currentTimeMillis()
                val startupResult = calculator.calculate(
                    fromTimestamp = lastTickTimestamp,
                    toTimestamp = now,
                    workProfile = profile,
                    carryInCents = carryInCents
                )
                carryInCents = startupResult.remainderCents
                emit(startupResult)

                // Record this moment as the new last tick
                preferencesRepository.saveLastTickTimestamp(now)

                // ── Foreground tick loop ─────────────────────────────────────
                // Keep ticking every TICK_INTERVAL_MS for as long as this
                // flow is collected (i.e., while the game screen is visible).
                while (true) {
                    delay(TICK_INTERVAL_MS)

                    val tickNow = System.currentTimeMillis()
                    val tickResult = calculator.calculate(
                        fromTimestamp = now,
                        toTimestamp = tickNow,
                        workProfile = profile,
                        carryInCents = carryInCents
                    )
                    carryInCents = tickResult.remainderCents
                    emit(tickResult)

                    preferencesRepository.saveLastTickTimestamp(tickNow)
                }
            }
    }
}
