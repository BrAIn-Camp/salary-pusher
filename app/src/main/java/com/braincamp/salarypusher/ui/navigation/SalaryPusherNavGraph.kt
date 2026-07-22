package com.braincamp.salarypusher.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.braincamp.salarypusher.ui.debug.DebugNotificationsScreen
import com.braincamp.salarypusher.ui.earnings.EarningsScreen
import com.braincamp.salarypusher.ui.game.GameScreen
import com.braincamp.salarypusher.ui.onboarding.OnboardingDenominationScreen
import com.braincamp.salarypusher.ui.onboarding.OnboardingNotificationsScreen
import com.braincamp.salarypusher.ui.onboarding.OnboardingSalaryScreen
import com.braincamp.salarypusher.ui.onboarding.OnboardingScheduleScreen
import com.braincamp.salarypusher.ui.onboarding.OnboardingViewModel
import com.braincamp.salarypusher.ui.onboarding.OnboardingWelcomeScreen
import com.braincamp.salarypusher.ui.settings.SettingsScreen

/**
 * Root navigation graph for Salary Pusher.
 *
 * Task 4.7: Start destination is determined by onboarding completion flag.
 * If onboarding is complete, navigate directly to the game screen.
 */
@Composable
fun SalaryPusherNavGraph(
    navController: NavHostController = rememberNavController(),
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    val isOnboardingComplete by onboardingViewModel.isOnboardingComplete.collectAsStateWithLifecycle()

    val startDestination = if (isOnboardingComplete) Routes.GAME
                           else Routes.ONBOARDING_WELCOME

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Routes.ONBOARDING_WELCOME) {
            OnboardingWelcomeScreen(
                onGetStarted = { navController.navigate(Routes.ONBOARDING_SALARY) }
            )
        }
        composable(Routes.ONBOARDING_SALARY) {
            OnboardingSalaryScreen(
                onNext = { hourlyCents ->
                    onboardingViewModel.hourlySalaryCents = hourlyCents
                    navController.navigate(Routes.ONBOARDING_SCHEDULE)
                }
            )
        }
        composable(Routes.ONBOARDING_SCHEDULE) {
            OnboardingScheduleScreen(
                onNext = { days, startHour, startMin, endHour, endMin ->
                    onboardingViewModel.workDays = days
                    onboardingViewModel.shiftStartHour = startHour
                    onboardingViewModel.shiftStartMinute = startMin
                    onboardingViewModel.shiftEndHour = endHour
                    onboardingViewModel.shiftEndMinute = endMin
                    navController.navigate(Routes.ONBOARDING_DENOMINATION)
                }
            )
        }
        composable(Routes.ONBOARDING_DENOMINATION) {
            OnboardingDenominationScreen(
                onNext = { denomination ->
                    onboardingViewModel.coinDenomination = denomination
                    navController.navigate(Routes.ONBOARDING_NOTIFICATIONS)
                }
            )
        }
        composable(Routes.ONBOARDING_NOTIFICATIONS) {
            OnboardingNotificationsScreen(
                onFinish = {
                    onboardingViewModel.completeOnboarding()
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.ONBOARDING_WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Main screens ──────────────────────────────────────────────────────
        composable(Routes.GAME) {
            GameScreen(
                onNavigateToEarnings = { navController.navigate(Routes.EARNINGS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.EARNINGS) {
            EarningsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDebug = { navController.navigate(Routes.DEBUG_NOTIFICATIONS) }
            )
        }
        composable(Routes.DEBUG_NOTIFICATIONS) {
            DebugNotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
