package com.braincamp.salarypusher.game.engine

import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.game.physics.CoinPhysicsConstants
import com.braincamp.salarypusher.game.physics.PhysicsConstants
import com.braincamp.salarypusher.game.physics.PusherConstants
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

/**
 * Exit zone a coin crossed when leaving the platform.
 */
enum class CoinExitZone { FRONT, LEFT, RIGHT }

/**
 * Mutable state of a single simulated coin.
 *
 * Positions use SceneView/Filament coordinates: +X right, +Y up, -Z forward.
 * The peg field is above the platform. Coins spawn at [PhysicsConstants.DROP_SPAWN_Y],
 * fall through peg rows, land on the platform, and are pushed forward (toward -Z/front).
 */
data class SimCoin(
    val id: String = UUID.randomUUID().toString(),
    val denomination: CoinDenomination,
    var x: Float,
    var y: Float,
    var z: Float,
    var vy: Float = 0f,         // vertical velocity (falls with gravity)
    var pegRowsHit: Int = 0,    // how many peg rows this coin has passed through
    var isOnPlatform: Boolean = false,
    var isAlive: Boolean = true
)

/**
 * Pure Kotlin coin simulation engine.
 *
 * Runs the physics for all coins: falling through the peg field, landing on the
 * platform, being pushed by the plate, and exiting via front or side edges.
 *
 * This is a simplified but convincing simulation:
 * - Gravity drives coins downward through the peg field
 * - Each peg row deflects coin X randomly (Plinko-style)
 * - Coins rest on the platform floor (simple height map approximation)
 * - The pusher plate advances all platform coins toward the front edge each frame
 * - Boundary exits fire events to the caller
 *
 * No external physics library required. All logic is plain Kotlin arithmetic,
 * making it fully testable without Android dependencies.
 *
 * @param onCoinExited  Callback fired when a coin leaves the play field.
 */
class CoinSimulation(
    private val onCoinExited: (coinId: String, zone: CoinExitZone) -> Unit
) {
    private val _coins = mutableListOf<SimCoin>()
    val coins: List<SimCoin> get() = _coins

    /** Current pusher plate Z position (moves between PUSH_RANGE_NEAR and PUSH_RANGE_FAR) */
    var pusherZ: Float = PusherConstants.PUSH_RANGE_FAR
        private set
    private var pusherDirection: Float = -1f    // -1 = moving toward front, +1 = toward back

    // Platform floor Y — coins rest here
    private val platformFloorY = 0f

    // Peg row Z positions — evenly spaced in the peg field
    private val pegRowZPositions: List<Float> = (0 until PhysicsConstants.PEG_ROWS.toInt()).map { row ->
        PhysicsConstants.DROP_SPAWN_Z + row * PhysicsConstants.PEG_SPACING_Z
    }

    // Half-width of the platform for boundary checks
    private val halfWidth = PhysicsConstants.PLATFORM_WIDTH / 2f

    /**
     * Spawn a new coin at the given normalized X tap position [-1, 1].
     * The coin enters at the top of the peg field.
     */
    fun spawnCoin(normalizedTapX: Float, denomination: CoinDenomination) {
        val spawnX = normalizedTapX * halfWidth * 0.9f  // 90% of platform width
        _coins.add(
            SimCoin(
                denomination = denomination,
                x = spawnX,
                y = PhysicsConstants.DROP_SPAWN_Y,
                z = PhysicsConstants.DROP_SPAWN_Z,
                vy = 0f
            )
        )
    }

    /**
     * Advance the simulation by [deltaSeconds].
     *
     * Called every frame from the SceneView onFrame callback.
     * Mutates all active coins in place and fires exit callbacks for any that leave.
     */
    fun step(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0f, 0.05f)  // clamp to prevent tunneling on slow frames

        // ── Advance pusher plate ──────────────────────────────────────────────
        pusherZ += pusherDirection * PusherConstants.PUSH_SPEED * dt
        if (pusherZ <= PusherConstants.PUSH_RANGE_NEAR) {
            pusherZ = PusherConstants.PUSH_RANGE_NEAR
            pusherDirection = 1f
        } else if (pusherZ >= PusherConstants.PUSH_RANGE_FAR) {
            pusherZ = PusherConstants.PUSH_RANGE_FAR
            pusherDirection = -1f
        }

        // ── Simulate each coin ────────────────────────────────────────────────
        val toRemove = mutableListOf<SimCoin>()

        for (coin in _coins) {
            if (!coin.isAlive) continue

            if (!coin.isOnPlatform) {
                // ── Falling through peg field ─────────────────────────────────
                coin.vy -= GRAVITY * dt
                coin.y += coin.vy * dt

                // Check if coin has passed through any new peg rows
                for (rowIndex in coin.pegRowsHit until pegRowZPositions.size) {
                    val rowZ = pegRowZPositions[rowIndex]
                    if (coin.z >= rowZ) {
                        // Coin has reached this peg row — deflect X randomly
                        val deflection = PEG_DEFLECTION_RANGE * (Random.nextFloat() * 2f - 1f)
                        coin.x = (coin.x + deflection).coerceIn(-halfWidth, halfWidth)
                        coin.pegRowsHit++
                    }
                }

                // Advance Z slowly (coins drift forward slightly as they fall)
                coin.z += PEG_Z_DRIFT * dt

                // Check if coin reached the platform floor
                if (coin.y <= platformFloorY + CoinPhysicsConstants.COIN_RADIUS) {
                    coin.y = platformFloorY + CoinPhysicsConstants.COIN_RADIUS
                    coin.vy = 0f
                    coin.isOnPlatform = true
                    // Place coin at a reasonable Z on the platform
                    coin.z = (PusherConstants.PUSH_RANGE_FAR * 0.6f).coerceIn(
                        PusherConstants.PUSH_RANGE_NEAR,
                        PusherConstants.PUSH_RANGE_FAR
                    )
                }
            } else {
                // ── On the platform — advance toward front edge ────────────────
                // All platform coins advance continuously toward the front.
                // The plate's forward motion is the driving force — push speed
                // applies whenever the plate is moving toward the front edge.
                if (pusherDirection < 0f) {
                    coin.z -= PLATFORM_PUSH_SPEED * dt
                }

                // Check front edge exit
                if (coin.z <= FRONT_EDGE_Z) {
                    coin.isAlive = false
                    toRemove.add(coin)
                    onCoinExited(coin.id, CoinExitZone.FRONT)
                    continue
                }
            }

            // ── Side edge exit (applies to both falling and platform coins) ──
            if (abs(coin.x) > halfWidth + SIDE_EXIT_TOLERANCE) {
                coin.isAlive = false
                toRemove.add(coin)
                val zone = if (coin.x > 0) CoinExitZone.RIGHT else CoinExitZone.LEFT
                onCoinExited(coin.id, zone)
            }
        }

        _coins.removeAll(toRemove)
    }

    /** Remove all coins — used on scene reset */
    fun clearAll() {
        _coins.clear()
    }

    companion object {
        /** Gravity acceleration in m/s² (downward = positive in our fall logic) */
        const val GRAVITY = 9.8f

        /** Max random X deflection per peg row hit */
        const val PEG_DEFLECTION_RANGE = 0.04f

        /** How fast coins drift in Z as they fall through the peg field */
        const val PEG_Z_DRIFT = 0.05f

        /** How fast the platform coins get pushed toward the front */
        const val PLATFORM_PUSH_SPEED = 0.04f

        /** Z coordinate of the front edge — coins past this are earned */
        const val FRONT_EDGE_Z = -0.02f

        /** How far past the side walls before a coin is considered exited */
        const val SIDE_EXIT_TOLERANCE = 0.01f
    }
}
