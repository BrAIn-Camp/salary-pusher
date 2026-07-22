package com.braincamp.salarypusher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for earning events.
 *
 * All queries that power the earnings display use [fromTimestamp] boundaries
 * derived from midnight today and Monday midnight.
 * The repository layer is responsible for calculating those boundaries.
 */
@Dao
interface EarningEventDao {

    @Insert
    suspend fun insert(event: EarningEventEntity): Long

    /**
     * Returns all events since [fromTimestamp] (inclusive), newest first.
     * Emits a new list whenever the table changes.
     */
    @Query("SELECT * FROM earning_events WHERE timestamp >= :fromTimestamp ORDER BY timestamp DESC")
    fun getEventsSince(fromTimestamp: Long): Flow<List<EarningEventEntity>>

    /**
     * Returns the sum of [amountCents] for all events since [fromTimestamp].
     * Returns null if there are no matching rows.
     */
    @Query("SELECT SUM(amountCents) FROM earning_events WHERE timestamp >= :fromTimestamp")
    suspend fun sumSince(fromTimestamp: Long): Long?

    /**
     * Returns the sum of all [amountCents] ever recorded.
     * Returns null if the table is empty.
     */
    @Query("SELECT SUM(amountCents) FROM earning_events")
    suspend fun sumAll(): Long?

    /**
     * Returns the total count of all earning events ever recorded.
     * Used for stats display and testing.
     */
    @Query("SELECT COUNT(*) FROM earning_events")
    suspend fun count(): Int
}
