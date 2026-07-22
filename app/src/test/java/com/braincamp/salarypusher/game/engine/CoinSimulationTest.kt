package com.braincamp.salarypusher.game.engine

import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.game.engine.CoinSimulation
import com.braincamp.salarypusher.game.physics.PhysicsConstants
import com.braincamp.salarypusher.game.physics.PusherConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class CoinSimulationTest {

    private lateinit var frontExits: MutableList<String>
    private lateinit var sideExits: MutableList<String>
    private lateinit var simulation: CoinSimulation

    @Before
    fun setUp() {
        frontExits = mutableListOf()
        sideExits = mutableListOf()
        simulation = CoinSimulation { coinId, zone ->
            when (zone) {
                CoinExitZone.FRONT -> frontExits.add(coinId)
                CoinExitZone.LEFT, CoinExitZone.RIGHT -> sideExits.add(coinId)
            }
        }
    }

    @Test
    fun spawnCoin_addsOneCoinToList() {
        simulation.spawnCoin(0f, CoinDenomination.QUARTER)
        assertEquals(1, simulation.coins.size)
    }

    @Test
    fun spawnCoin_centerTap_spawnsAtCenterX() {
        simulation.spawnCoin(0f, CoinDenomination.QUARTER)
        assertEquals(0f, simulation.coins.first().x, 0.001f)
    }

    @Test
    fun spawnCoin_leftTap_spawnsAtNegativeX() {
        simulation.spawnCoin(-1f, CoinDenomination.QUARTER)
        assertTrue(simulation.coins.first().x < 0f)
    }

    @Test
    fun spawnCoin_rightTap_spawnsAtPositiveX() {
        simulation.spawnCoin(1f, CoinDenomination.QUARTER)
        assertTrue(simulation.coins.first().x > 0f)
    }

    @Test
    fun step_coinFalls_yDecreases() {
        simulation.spawnCoin(0f, CoinDenomination.QUARTER)
        val initialY = simulation.coins.first().y
        simulation.step(0.1f)
        assertTrue(simulation.coins.first().y < initialY)
    }

    @Test
    fun pusherPlate_oscillatesBetweenBounds() {
        // Step enough times to observe the pusher reversing direction
        val positions = mutableSetOf<Float>()
        repeat(200) {
            simulation.step(0.05f)
            positions.add(simulation.pusherZ)
        }
        // Should have visited both extremes
        assertTrue(positions.any { it <= PusherConstants.PUSH_RANGE_NEAR + 0.001f })
        assertTrue(positions.any { it >= PusherConstants.PUSH_RANGE_FAR - 0.001f })
    }

    @Test
    fun coinOnPlatform_crossingFrontEdge_firesEarnedEvent() {
        simulation.spawnCoin(0f, CoinDenomination.QUARTER)
        val coin = simulation.coins.first()
        coin.isOnPlatform = true
        coin.z = CoinSimulation.FRONT_EDGE_Z + 0.05f   // 5cm behind front edge
        coin.y = 0.01f

        // Step until coin exits — at PLATFORM_PUSH_SPEED 0.04 m/s it takes ~1.25s
        repeat(200) { simulation.step(0.05f) }

        assertTrue("Expected front exit event", frontExits.isNotEmpty())
    }

    @Test
    fun coinExitingFrontEdge_removedFromCoinList() {
        simulation.spawnCoin(0f, CoinDenomination.QUARTER)
        val coin = simulation.coins.first()
        val coinId = coin.id
        coin.isOnPlatform = true
        coin.z = CoinSimulation.FRONT_EDGE_Z + 0.05f
        coin.y = 0.01f

        repeat(200) { simulation.step(0.05f) }

        assertTrue(simulation.coins.none { it.id == coinId })
    }

    @Test
    fun clearAll_removesAllCoins() {
        simulation.spawnCoin(0f, CoinDenomination.QUARTER)
        simulation.spawnCoin(0.5f, CoinDenomination.DOLLAR)
        simulation.clearAll()
        assertEquals(0, simulation.coins.size)
    }

    @Test
    fun dropQueueViewModel_recycledCoin_incrementsQueue() {
        val vm = DropQueueViewModel()
        vm.onCoinAccrued(5)
        vm.onCoinDropped()  // queue: 4
        vm.onCoinRecycled() // queue: 5
        assertEquals(5, vm.dropQueueCount.value)
    }
}
