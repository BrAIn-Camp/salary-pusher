package com.braincamp.salarypusher.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Placeholder — full implementation in Task 4.2 */
@Composable
fun OnboardingWelcomeScreen(onGetStarted: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to Salary Pusher 🪙")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onGetStarted) { Text("Get Started") }
        }
    }
}

/** Placeholder — full implementation in Task 4.3 */
@Composable
fun OnboardingSalaryScreen(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Enter Your Salary")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNext) { Text("Next") }
        }
    }
}

/** Placeholder — full implementation in Task 4.4 */
@Composable
fun OnboardingScheduleScreen(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Set Your Work Schedule")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNext) { Text("Next") }
        }
    }
}

/** Placeholder — full implementation in Task 4.5 */
@Composable
fun OnboardingDenominationScreen(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Choose Your Coin Denomination")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNext) { Text("Next") }
        }
    }
}

/** Placeholder — full implementation in Task 4.6 */
@Composable
fun OnboardingNotificationsScreen(onFinish: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Enable Notifications")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onFinish) { Text("Enable Notifications") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onFinish) { Text("Skip for Now") }
        }
    }
}
