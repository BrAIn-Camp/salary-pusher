package com.braincamp.salarypusher.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.domain.model.EarningEvent

/**
 * Room entity representing a single coin collected from the front edge.
 *
 * [denominationOrdinal] stores [CoinDenomination.ordinal] — never store
 * the enum name as it is subject to refactoring.
 */
@Entity(tableName = "earning_events")
data class EarningEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val timestamp: Long,
    val denominationOrdinal: Int
) {
    fun toDomain(): EarningEvent = EarningEvent(
        id = id,
        amountCents = amountCents,
        timestamp = timestamp,
        denomination = CoinDenomination.entries[denominationOrdinal]
    )

    companion object {
        fun fromDomain(event: EarningEvent): EarningEventEntity = EarningEventEntity(
            id = event.id,
            amountCents = event.amountCents,
            timestamp = event.timestamp,
            denominationOrdinal = event.denomination.ordinal
        )
    }
}
