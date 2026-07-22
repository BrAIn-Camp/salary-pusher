package com.braincamp.salarypusher.domain.model

/**
 * Available coin denominations.
 *
 * [valueInCents] is the real-world value of one coin.
 * [displayName] is the human-readable label shown in UI.
 * [playStyleHint] is shown on the denomination selection screen.
 *
 * These values must never be changed — they are persisted as ordinals in DataStore
 * and as integers in the Room database.
 */
enum class CoinDenomination(
    val valueInCents: Int,
    val displayName: String,
    val playStyleHint: String
) {
    PENNY(
        valueInCents = 1,
        displayName = "Penny",
        playStyleHint = "Most coins — longest sessions — most drops per shift"
    ),
    NICKEL(
        valueInCents = 5,
        displayName = "Nickel",
        playStyleHint = "Lots of coins — great for a full workday"
    ),
    DIME(
        valueInCents = 10,
        displayName = "Dime",
        playStyleHint = "Balanced — a solid choice for most players"
    ),
    QUARTER(
        valueInCents = 25,
        displayName = "Quarter",
        playStyleHint = "Fewer coins — each drop carries more weight"
    ),
    DOLLAR(
        valueInCents = 100,
        displayName = "Dollar",
        playStyleHint = "Fewest coins — every drop is a big moment"
    );

    /**
     * Formats a coin count as a display string.
     * e.g. formatCount(3) on QUARTER -> "3 quarters"
     */
    fun formatCount(count: Int): String {
        val label = when (this) {
            PENNY  -> if (count == 1) "penny" else "pennies"
            NICKEL -> if (count == 1) "nickel" else "nickels"
            DIME   -> if (count == 1) "dime" else "dimes"
            QUARTER -> if (count == 1) "quarter" else "quarters"
            DOLLAR -> if (count == 1) "dollar" else "dollars"
        }
        return "$count $label"
    }
}
