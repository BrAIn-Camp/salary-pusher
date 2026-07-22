package com.braincamp.salarypusher.notifications.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives BOOT_COMPLETED broadcast and reschedules WorkManager notification tasks.
 * Full implementation added in Task 7.3 (notification scheduling).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO Task 7.3: reschedule all WorkManager notification workers here
        }
    }
}
