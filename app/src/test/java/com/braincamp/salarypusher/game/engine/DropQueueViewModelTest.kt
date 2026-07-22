package com.braincamp.salarypusher.game.engine

import com.braincamp.salarypusher.domain.model.CoinDenomination
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [DropQueueViewModel].
 *
 * Tests the core invariants:
 * - Queue never goes negative
 * - Accrual, drop, and recycle all update count correctly
 * - Drop returns false (not true) on an empty queue
 */
class DropQueueViewModelTest {

    private lateinit var viewModel: DropQueueViewModel

    @Before
    fun setUp() {
        viewModel = DropQueueViewModel()
    }

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun initialQueueCount_isZero() {
        assertEquals(0, viewModel.dropQueueCount.value)
    }

    @Test
    fun initialDenomination_isQuarter() {
        assertEquals(CoinDenomination.QUARTER, viewModel.currentDenomination.value)
    }

    // ─── onCoinAccrued ────────────────────────────────────────────────────────

    @Test
    fun onCoinAccrued_fiveCoins_queueIncreasesBy5() {
        viewModel.onCoinAccrued(5)
        assertEquals(5, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinAccrued_multipleTimes_countsAccumulate() {
        viewModel.onCoinAccrued(10)
        viewModel.onCoinAccrued(10)
        assertEquals(20, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinAccrued_zeroCoins_queueUnchanged() {
        viewModel.onCoinAccrued(5)
        viewModel.onCoinAccrued(0)
        assertEquals(5, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinAccrued_negativeCount_queueUnchanged() {
        viewModel.onCoinAccrued(5)
        viewModel.onCoinAccrued(-3)
        assertEquals(5, viewModel.dropQueueCount.value)
    }

    // ─── onCoinDropped ────────────────────────────────────────────────────────

    @Test
    fun onCoinDropped_withCoinsInQueue_returnsTrue() {
        viewModel.onCoinAccrued(1)
        val result = viewModel.onCoinDropped()
        assertTrue(result)
    }

    @Test
    fun onCoinDropped_withCoinsInQueue_decrementsByOne() {
        viewModel.onCoinAccrued(5)
        viewModel.onCoinDropped()
        assertEquals(4, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinDropped_emptyQueue_returnsFalse() {
        val result = viewModel.onCoinDropped()
        assertFalse(result)
    }

    @Test
    fun onCoinDropped_emptyQueue_countRemainsZero() {
        viewModel.onCoinDropped()
        assertEquals(0, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinDropped_emptyQueue_doesNotGoNegative() {
        repeat(10) { viewModel.onCoinDropped() }
        assertEquals(0, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinDropped_drainEntireQueue_countReachesZero() {
        viewModel.onCoinAccrued(3)
        viewModel.onCoinDropped()
        viewModel.onCoinDropped()
        viewModel.onCoinDropped()
        assertEquals(0, viewModel.dropQueueCount.value)
    }

    // ─── onCoinRecycled ───────────────────────────────────────────────────────

    @Test
    fun onCoinRecycled_incrementsQueueByOne() {
        viewModel.onCoinAccrued(5)
        viewModel.onCoinDropped()  // queue: 4
        viewModel.onCoinRecycled() // queue: 5
        assertEquals(5, viewModel.dropQueueCount.value)
    }

    @Test
    fun onCoinRecycled_onEmptyQueue_queueBecomesOne() {
        viewModel.onCoinRecycled()
        assertEquals(1, viewModel.dropQueueCount.value)
    }

    // ─── onDenominationChanged ────────────────────────────────────────────────

    @Test
    fun onDenominationChanged_updatesDenomination() {
        viewModel.onDenominationChanged(CoinDenomination.DOLLAR)
        assertEquals(CoinDenomination.DOLLAR, viewModel.currentDenomination.value)
    }

    @Test
    fun onDenominationChanged_doesNotClearQueue() {
        viewModel.onCoinAccrued(10)
        viewModel.onDenominationChanged(CoinDenomination.PENNY)
        assertEquals(10, viewModel.dropQueueCount.value)
    }

    // ─── Combined scenarios ───────────────────────────────────────────────────

    @Test
    fun accrueDropRecycle_maintainsCorrectCount() {
        viewModel.onCoinAccrued(10) // queue: 10
        viewModel.onCoinDropped()   // queue: 9
        viewModel.onCoinDropped()   // queue: 8
        viewModel.onCoinRecycled()  // queue: 9 (one side-exit returned)
        viewModel.onCoinAccrued(5)  // queue: 14
        viewModel.onCoinDropped()   // queue: 13
        assertEquals(13, viewModel.dropQueueCount.value)
    }
}
