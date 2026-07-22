package com.braincamp.salarypusher.ui.navigation

import androidx.compose.runtime.Composable
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
import com.braincamp.salarypusher.ui.onboarding.OnboardingWelcomeScreen
import com.braincamp.salarypusher.ui.settings.SettingsScreen

/**
 * Root navigation graph for Salary Pusher.
 *
 * Start destination is determined by the onboarding completion flag (Task 4.7).
 * For now, always starts at the welcome screen.
 *
 * Full onboarding-skip logic wired in Task 4.7.
 */
@Composable
fun SalaryPusherNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.ONBOARDING_WELCOME
) {
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
                onNext = { navController.navigate(Routes.ONBOARDING_SCHEDULE) }
            )
        }
        composable(Routes.ONBOARDING_SCHEDULE) {
            OnboardingScheduleScreen(
                onNext = { navController.navigate(Routes.ONBOARDING_DENOMINATION) }
            )
        }
        composable(Routes.ONBOARDING_DENOMINATION) {
            OnboardingDenominationScreen(
                onNext = { navController.navigate(Routes.ONBOARDING_NOTIFICATIONS) }
            )
        }
        composable(Routes.ONBOARDING_NOTIFICATIONS) {
            OnboardingNotificationsScreen(
                onFinish = {
                    navController.navigate(Routes.GAME) {
                        // Clear the entire onboarding stack so Back doesn't return to it
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
