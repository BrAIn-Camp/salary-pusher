package com.braincamp.salarypusher.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.domain.model.WorkProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

// Top-level extension — creates a single DataStore instance for the app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "salary_pusher_prefs"
)

/**
 * Repository for all DataStore preferences.
 *
 * Provides typed read/write access to user preferences.
 * All defaults are defined here — callers never deal with nulls from missing keys.
 *
 * Default work profile: Mon-Fri, 9 AM - 5 PM, $0/hr (forces onboarding), QUARTER denomination.
 */
class UserPreferencesRepository(private val context: Context) {

    // ── Onboarding ────────────────────────────────────────────────────────────

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.ONBOARDING_COMPLETE] = true
        }
    }

    // ── Work profile ──────────────────────────────────────────────────────────

    /**
     * Emits a [WorkProfile] whenever any preference changes.
     * Returns null if onboarding has not been completed (salary will be 0).
     *
     * Callers that need a non-null profile should use [getWorkProfileOrNull]
     * and check before proceeding.
     */
    val workProfileFlow: Flow<WorkProfile> = context.dataStore.data.map { prefs ->
        WorkProfile(
            hourlySalaryCents = prefs[PreferenceKeys.HOURLY_SALARY_CENTS] ?: 0L,
            workDays = deserializeWorkDays(prefs[PreferenceKeys.WORK_DAYS]),
            shiftStartHour = prefs[PreferenceKeys.SHIFT_START_HOUR] ?: 9,
            shiftStartMinute = prefs[PreferenceKeys.SHIFT_START_MINUTE] ?: 0,
            shiftEndHour = prefs[PreferenceKeys.SHIFT_END_HOUR] ?: 17,
            shiftEndMinute = prefs[PreferenceKeys.SHIFT_END_MINUTE] ?: 0,
            coinDenomination = CoinDenomination.entries[
                prefs[PreferenceKeys.COIN_DENOMINATION] ?: CoinDenomination.QUARTER.ordinal
            ]
        )
    }

    suspend fun saveWorkProfile(profile: WorkProfile) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.HOURLY_SALARY_CENTS] = profile.hourlySalaryCents
            prefs[PreferenceKeys.WORK_DAYS] = serializeWorkDays(profile.workDays)
            prefs[PreferenceKeys.SHIFT_START_HOUR] = profile.shiftStartHour
            prefs[PreferenceKeys.SHIFT_START_MINUTE] = profile.shiftStartMinute
            prefs[PreferenceKeys.SHIFT_END_HOUR] = profile.shiftEndHour
            prefs[PreferenceKeys.SHIFT_END_MINUTE] = profile.shiftEndMinute
            prefs[PreferenceKeys.COIN_DENOMINATION] = profile.coinDenomination.ordinal
        }
    }

    // ── Salary clock ──────────────────────────────────────────────────────────

    val lastTickTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.LAST_TICK_TIMESTAMP] ?: System.currentTimeMillis()
    }

    suspend fun saveLastTickTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.LAST_TICK_TIMESTAMP] = timestamp
        }
    }

    // ── Notification preferences ──────────────────────────────────────────────

    val notificationPrefs: Flow<NotificationPreferences> = context.dataStore.data.map { prefs ->
        NotificationPreferences(
            shiftStart = prefs[PreferenceKeys.NOTIF_SHIFT_START] ?: true,
            coinsAccruing = prefs[PreferenceKeys.NOTIF_COINS_ACCRUING] ?: true,
            boardStatus = prefs[PreferenceKeys.NOTIF_BOARD_STATUS] ?: true,
            earningsSummary = prefs[PreferenceKeys.NOTIF_EARNINGS_SUMMARY] ?: true,
            lunchNudge = prefs[PreferenceKeys.NOTIF_LUNCH_NUDGE] ?: true,
            weeklyPayday = prefs[PreferenceKeys.NOTIF_WEEKLY_PAYDAY] ?: true
        )
    }

    suspend fun setNotificationEnabled(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[key] = enabled
        }
    }

    // ── Serialization helpers ─────────────────────────────────────────────────

    private fun serializeWorkDays(days: Set<DayOfWeek>): String =
        days.map { it.value }.sorted().joinToString(",")

    private fun deserializeWorkDays(serialized: String?): Set<DayOfWeek> {
        if (serialized.isNullOrBlank()) {
            // Default: Mon-Fri
            return setOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )
        }
        return serialized.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .map { DayOfWeek.of(it) }
            .toSet()
    }
}

/**
 * Notification on/off preferences for each channel.
 * All default to true — opt-out model.
 */
data class NotificationPreferences(
    val shiftStart: Boolean,
    val coinsAccruing: Boolean,
    val boardStatus: Boolean,
    val earningsSummary: Boolean,
    val lunchNudge: Boolean,
    val weeklyPayday: Boolean
)
