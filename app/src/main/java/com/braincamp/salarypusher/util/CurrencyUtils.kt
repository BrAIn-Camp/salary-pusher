package com.braincamp.salarypusher.util

/**
 * Formats a cent amount as a display string.
 *
 * Examples:
 *   formatCents(0)       -> "$0.00"
 *   formatCents(25)      -> "$0.25"
 *   formatCents(100)     -> "$1.00"
 *   formatCents(123456)  -> "$1,234.56"
 */
fun formatCents(cents: Long): String {
    val dollars = cents / 100
    val remainingCents = cents % 100
    return "$%,d.%02d".format(dollars, remainingCents)
}

/**
 * Converts an annual salary in cents to an hourly rate in cents.
 * Uses 2080 work hours per year (52 weeks × 40 hours).
 */
fun annualCentsToHourlyCents(annualCents: Long): Long = annualCents / 2080

/**
 * Converts an hourly rate in cents to an annual salary in cents.
 */
fun hourlyCentsToAnnualCents(hourlyCents: Long): Long = hourlyCents * 2080
