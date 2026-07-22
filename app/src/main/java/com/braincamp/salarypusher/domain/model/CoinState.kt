package com.braincamp.salarypusher.domain.model

/**
 * Represents the live state of a single coin in the game world.
 *
 * This is NOT persisted — it is an in-memory game state object managed by
 * the game engine. Created when a coin enters the peg field, destroyed when
 * it exits via the front edge (earned) or side edges (recycled).
 *
 * [id] is a UUID string assigned at spawn time.
 */
data class CoinState(
    val id: String,
    val denomination: CoinDenomination
)
