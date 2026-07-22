package com.braincamp.salarypusher.game.engine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.braincamp.salarypusher.data.datastore.UserPreferencesRepository
import com.braincamp.salarypusher.data.db.SalaryPusherDatabase
import com.braincamp.salarypusher.data.repository.EarningsRepository
import com.braincamp.salarypusher.domain.model.CoinDenomination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central ViewModel for the game screen.
 *
 * Coordinates:
 * - [DropQueueViewModel] — coins available to drop
 * - [CoinSimulation] — physics simulation state
 * - [SalaryClock] — accrual of new coins from salary
 * - [EarningsRepository] — persisting front-edge earnings
 *
 * The game screen observes [coins] for rendering and [dropQueueCount] for the HUD.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferencesRepository(application)
    private val db = SalaryPusherDatabase.getInstance(application)
    private val earningsRepository = EarningsRepository(db.earningEventDao())
    private val calculator = CoinAccrualCalculator()
    private val salaryClock = SalaryClock(calculator, prefs)

    val dropQueueViewModel = DropQueueViewModel()

    // ── Simulation ────────────────────────────────────────────────────────────

    private val simulation = CoinSimulation { coinId, zone ->
        when (zone) {
            CoinExitZone.FRONT -> onCoinEarned()
            CoinExitZone.LEFT, CoinExitZone.RIGHT -> dropQueueViewModel.onCoinRecycled()
        }
    }

    private val _coins = MutableStateFlow<List<SimCoin>>(emptyList())
    val coins: StateFlow<List<SimCoin>> = _coins.asStateFlow()

    private val _pusherZ = MutableStateFlow(0f)
    val pusherZ: StateFlow<Float> = _pusherZ.asStateFlow()

    // ── HUD state ─────────────────────────────────────────────────────────────

    val dropQueueCount = dropQueueViewModel.dropQueueCount
    val currentDenomination = dropQueueViewModel.currentDenomination

    // ── Salary clock ──────────────────────────────────────────────────────────

    init {
        // Collect salary clock accruals and feed into drop queue
        viewModelScope.launch {
            salaryClock.coinAccrualFlow.collect { result ->
                if (result.coins > 0) {
                    dropQueueViewModel.onCoinAccrued(result.coins)
                }
            }
        }

        // Keep denomination in sync with saved preferences
        viewModelScope.launch {
            prefs.workProfileFlow.collect { profile ->
                dropQueueViewModel.onDenominationChanged(profile.coinDenomination)
            }
        }
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    /**
     * Called every frame from the SceneView onFrame callback.
     * [frameTimeNanos] is the current frame time in nanoseconds.
     */
    private var lastFrameTimeNanos: Long? = null

    fun onFrame(frameTimeNanos: Long) {
        val prev = lastFrameTimeNanos
        lastFrameTimeNanos = frameTimeNanos
        val deltaSeconds = if (prev != null) {
            ((frameTimeNanos - prev) / 1_000_000_000f).coerceIn(0f, 0.05f)
        } else 0f

        simulation.step(deltaSeconds)
        _coins.value = simulation.coins.toList()
        _pusherZ.value = simulation.pusherZ
    }

    // ── Player interaction ────────────────────────────────────────────────────

    /**
     * Called when the player taps the game screen.
     * [normalizedTapX] is the tap X position mapped to [-1, 1] across the screen width.
     */
    fun onTap(normalizedTapX: Float) {
        val denomination = dropQueueViewModel.currentDenomination.value
        if (dropQueueViewModel.onCoinDropped()) {
            simulation.spawnCoin(normalizedTapX, denomination)
        }
    }

    // ── Earnings ──────────────────────────────────────────────────────────────

    private fun onCoinEarned() {
        val denomination = dropQueueViewModel.currentDenomination.value
        viewModelScope.launch {
            earningsRepository.recordEarning(denomination)
        }
    }
}
