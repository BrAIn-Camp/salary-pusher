package com.braincamp.salarypusher.game.engine

import androidx.lifecycle.ViewModel
import com.braincamp.salarypusher.domain.model.CoinDenomination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages the live state of the drop queue — coins the player has available to drop.
 *
 * The drop queue is fed by two sources:
 * 1. [onCoinAccrued] — salary clock ticks during work hours.
 * 2. [onCoinRecycled] — coins that fell off the side edges of the pusher.
 *
 * The queue is depleted by [onCoinDropped], which is called when the player
 * taps the game screen to release a coin.
 *
 * Invariant: [dropQueueCount] never goes negative.
 *
 * This ViewModel does not persist state — the drop queue resets on app restart.
 * Coins accrued while the app was closed are recalculated on startup by
 * [SalaryClock] and added via [onCoinAccrued].
 */
class DropQueueViewModel : ViewModel() {

    private val _dropQueueCount = MutableStateFlow(0)
    val dropQueueCount: StateFlow<Int> = _dropQueueCount.asStateFlow()

    private val _currentDenomination = MutableStateFlow(CoinDenomination.QUARTER)
    val currentDenomination: StateFlow<CoinDenomination> = _currentDenomination.asStateFlow()

    /**
     * Adds [count] coins to the drop queue.
     * Called by the salary clock on each tick.
     */
    fun onCoinAccrued(count: Int) {
        if (count <= 0) return
        _dropQueueCount.update { it + count }
    }

    /**
     * Attempts to consume one coin from the drop queue for a player drop.
     *
     * @return true if a coin was available and consumed, false if the queue was empty.
     */
    fun onCoinDropped(): Boolean {
        var wasDropped = false
        _dropQueueCount.update { current ->
            if (current > 0) {
                wasDropped = true
                current - 1
            } else {
                current
            }
        }
        return wasDropped
    }

    /**
     * Returns a coin to the drop queue after it exits a side edge.
     * This coin was already spent from the queue, so it is simply returned.
     */
    fun onCoinRecycled() {
        _dropQueueCount.update { it + 1 }
    }

    /**
     * Updates the active denomination.
     * Called when the player changes denomination in settings.
     * Does NOT clear the existing queue — denomination changes apply to future accruals.
     */
    fun onDenominationChanged(denomination: CoinDenomination) {
        _currentDenomination.value = denomination
    }
}
