package com.braincamp.salarypusher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.braincamp.salarypusher.ui.navigation.SalaryPusherNavGraph

/**
 * Main entry point for Salary Pusher.
 *
 * Hosts the Compose navigation graph.
 * Start destination logic (onboarding vs game) added in Task 4.7.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    SalaryPusherNavGraph()
                }
            }
        }
    }
}
