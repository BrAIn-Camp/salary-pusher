package com.braincamp.salarypusher

import android.app.Application

/**
 * Application class for Salary Pusher.
 *
 * Responsibilities:
 * - Notification channel registration (added in Task 7.1)
 * - Dependency initialization
 */
class SalaryPusherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Notification channels registered here in Task 7.1
    }
}
