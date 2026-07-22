package com.braincamp.salarypusher.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.braincamp.salarypusher.data.datastore.UserPreferencesRepository
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.domain.model.WorkProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * ViewModel shared across all onboarding screens.
 *
 * Holds the in-progress work profile being built during onboarding.
 * Persists the final profile to DataStore when onboarding completes.
 */
class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferencesRepository(application)

    // Observe onboarding completion — used by MainActivity to skip onboarding
    val isOnboardingComplete = prefs.isOnboardingComplete
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // In-progress values collected across screens
    var hourlySalaryCents: Long = 0L
    var workDays: Set<DayOfWeek> = setOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    )
    var shiftStartHour: Int = 9
    var shiftStartMinute: Int = 0
    var shiftEndHour: Int = 17
    var shiftEndMinute: Int = 0
    var coinDenomination: CoinDenomination = CoinDenomination.QUARTER

    /**
     * Saves the completed work profile and marks onboarding as done.
     * Call this when the final onboarding screen is dismissed.
     */
    fun completeOnboarding() {
        val profile = WorkProfile(
            hourlySalaryCents = hourlySalaryCents,
            workDays = workDays,
            shiftStartHour = shiftStartHour,
            shiftStartMinute = shiftStartMinute,
            shiftEndHour = shiftEndHour,
            shiftEndMinute = shiftEndMinute,
            coinDenomination = coinDenomination
        )
        viewModelScope.launch {
            prefs.saveWorkProfile(profile)
            prefs.setOnboardingComplete()
        }
    }
}
