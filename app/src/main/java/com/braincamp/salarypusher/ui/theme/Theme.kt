package com.braincamp.salarypusher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SalaryPusherColorScheme = darkColorScheme(
    primary          = CoinGold,
    onPrimary        = ArcadeDark,
    primaryContainer = CoinGoldDark,
    onPrimaryContainer = CoinGoldLight,

    secondary        = CoinSilver,
    onSecondary      = ArcadeDark,
    secondaryContainer = CoinSilverDark,

    background       = ArcadeDark,
    onBackground     = TextPrimary,

    surface          = ArcadeSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = ArcadeElevated,
    onSurfaceVariant = TextSecondary,

    error            = ErrorRed,
    onError          = TextPrimary
)

/**
 * The root Compose theme for Salary Pusher.
 *
 * Apply this at the top of every screen composable and in all preview annotations.
 * Never hardcode colors — always use MaterialTheme.colorScheme.
 */
@Composable
fun SalaryPusherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SalaryPusherColorScheme,
        typography  = SalaryPusherTypography,
        content     = content
    )
}
