package com.braincamp.salarypusher.ui.game

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

/** Placeholder — full implementation in Phase 5 */
@Composable
fun GameScreen(
    onNavigateToEarnings: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎰 Coin Pusher — Coming in Phase 5")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToEarnings) { Text("Earnings") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToSettings) { Text("Settings") }
        }
    }
}
