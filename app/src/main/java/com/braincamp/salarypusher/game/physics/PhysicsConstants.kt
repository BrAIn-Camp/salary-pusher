package com.braincamp.salarypusher.game.physics

/**
 * Physics constants for the coin pusher world.
 *
 * All values are in SI units (meters, kg, seconds).
 * Adjust these to tune the feel of the simulation — see comments for guidance.
 */
object PhysicsConstants {
    // ── Platform ──────────────────────────────────────────────────────────────
    /** Higher = coins slide less freely on the platform */
    const val PLATFORM_FRICTION     = 0.6f
    /** Lower = coins don't bounce when hitting the platform */
    const val PLATFORM_RESTITUTION  = 0.1f

    // ── Pegs ──────────────────────────────────────────────────────────────────
    /** Moderate bounce off pegs keeps drops feeling satisfying */
    const val PEG_RESTITUTION       = 0.45f
    const val PEG_FRICTION          = 0.2f

    // ── Walls ─────────────────────────────────────────────────────────────────
    const val WALL_RESTITUTION      = 0.3f
    const val WALL_FRICTION         = 0.4f

    // ── Scene dimensions (meters) ─────────────────────────────────────────────
    /** Width of the coin pusher platform */
    const val PLATFORM_WIDTH        = 0.5f
    /** Depth of the coin pusher platform (front to back) */
    const val PLATFORM_DEPTH        = 0.4f
    /** Height of the side and back walls */
    const val WALL_HEIGHT           = 0.05f
    /** Wall thickness */
    const val WALL_THICKNESS        = 0.01f

    // ── Peg field ─────────────────────────────────────────────────────────────
    /** Radius of each cylindrical peg */
    const val PEG_RADIUS            = 0.008f
    /** Height of each peg */
    const val PEG_HEIGHT            = 0.015f
    /** Horizontal spacing between pegs in a row */
    const val PEG_SPACING_X         = 0.055f
    /** Vertical spacing between peg rows */
    const val PEG_SPACING_Z         = 0.05f
    /** Number of peg columns */
    const val PEG_COLS              = 8
    /** Number of peg rows */
    const val PEG_ROWS              = 5
    /** Y position of the peg field above the platform */
    const val PEG_FIELD_Y           = 0.35f

    // ── Drop zone ─────────────────────────────────────────────────────────────
    /** Y height at which coins are spawned */
    const val DROP_SPAWN_Y          = 0.6f
    /** Z position of spawn (top of peg field, towards back) */
    const val DROP_SPAWN_Z          = -0.15f
}

/**
 * Physics constants specific to coins.
 */
object CoinPhysicsConstants {
    /** Coin radius in meters — roughly the size of a US quarter */
    const val COIN_RADIUS           = 0.012f
    /** Coin thickness in meters */
    const val COIN_THICKNESS        = 0.002f
    /** Coin mass in kg (~5 grams) */
    const val COIN_MASS             = 0.005f
    /** Moderate bounce — feels like a real coin, not a rubber ball */
    const val COIN_RESTITUTION      = 0.3f
    /** Moderate friction — coins slide but don't skate */
    const val COIN_FRICTION         = 0.5f
}

/**
 * Pusher plate movement constants.
 */
object PusherConstants {
    /** Plate movement speed in meters per second */
    const val PUSH_SPEED            = 0.06f
    /** Z position of plate at its closest point to the front edge */
    const val PUSH_RANGE_NEAR       = 0.05f
    /** Z position of plate at its furthest point from front edge */
    const val PUSH_RANGE_FAR        = 0.28f
    /** Plate width — matches platform width */
    const val PLATE_WIDTH           = PhysicsConstants.PLATFORM_WIDTH
    /** Plate thickness */
    const val PLATE_THICKNESS       = 0.01f
    /** Plate height (how tall the wall of the pusher is) */
    const val PLATE_HEIGHT          = 0.03f
}
