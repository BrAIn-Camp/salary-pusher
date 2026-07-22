package com.braincamp.salarypusher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Main entry point for Salary Pusher.
 *
 * Navigation graph and full UI wired up in Task 1.4.
 * This file should not grow significantly beyond hosting the Compose content root.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlaceholderScreen()
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Salary Pusher 🪙")
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreview() {
    MaterialTheme {
        PlaceholderScreen()
    }
}
