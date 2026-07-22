package com.braincamp.salarypusher.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * All DataStore preference keys used by the app.
 *
 * Keys are defined here and nowhere else — never construct key names inline.
 *
 * Naming convention: module_field (snake_case)
 */
object PreferenceKeys {
    // Onboarding
    val ONBOARDING_COMPLETE         = booleanPreferencesKey("onboarding_complete")

    // Work profile — salary
    val HOURLY_SALARY_CENTS         = longPreferencesKey("hourly_salary_cents")

    // Work profile — schedule
    // Work days stored as a comma-separated string of DayOfWeek ordinals e.g. "1,2,3,4,5"
    val WORK_DAYS                   = stringPreferencesKey("work_days")
    val SHIFT_START_HOUR            = intPreferencesKey("shift_start_hour")
    val SHIFT_START_MINUTE          = intPreferencesKey("shift_start_minute")
    val SHIFT_END_HOUR              = intPreferencesKey("shift_end_hour")
    val SHIFT_END_MINUTE            = intPreferencesKey("shift_end_minute")

    // Work profile — coin denomination (stored as CoinDenomination.ordinal)
    val COIN_DENOMINATION           = intPreferencesKey("coin_denomination")

    // Salary clock — last tick timestamp (epoch millis)
    val LAST_TICK_TIMESTAMP         = longPreferencesKey("last_tick_timestamp")

    // Notification preferences — per channel on/off
    val NOTIF_SHIFT_START           = booleanPreferencesKey("notif_shift_start")
    val NOTIF_COINS_ACCRUING        = booleanPreferencesKey("notif_coins_accruing")
    val NOTIF_BOARD_STATUS          = booleanPreferencesKey("notif_board_status")
    val NOTIF_EARNINGS_SUMMARY      = booleanPreferencesKey("notif_earnings_summary")
    val NOTIF_LUNCH_NUDGE           = booleanPreferencesKey("notif_lunch_nudge")
    val NOTIF_WEEKLY_PAYDAY         = booleanPreferencesKey("notif_weekly_payday")
}
