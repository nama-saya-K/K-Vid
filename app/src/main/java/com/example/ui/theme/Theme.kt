package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = GoldPrimaryContainer,
    onPrimaryContainer = GoldOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberOnContainer,
    tertiary = SageTertiary,
    onTertiary = SageOnTertiary,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = SageOnContainer,
    background = NightBackground,
    onBackground = NightTextPrimary,
    surface = NightSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextSecondary,
    outline = NightOutline
)

private val LightColorScheme = lightColorScheme(
    primary = EspressoPrimary,
    onPrimary = EspressoOnPrimary,
    primaryContainer = EspressoPrimaryContainer,
    onPrimaryContainer = EspressoOnPrimaryContainer,
    secondary = TerracottaSecondary,
    onSecondary = TerracottaOnSecondary,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = TerracottaOnContainer,
    tertiary = ForestTertiary,
    onTertiary = ForestOnTertiary,
    tertiaryContainer = ForestContainer,
    onTertiaryContainer = ForestOnContainer,
    background = ParchmentBackground,
    onBackground = InkTextPrimary,
    surface = ParchmentSurface,
    onSurface = InkTextPrimary,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = InkTextSecondary,
    outline = WarmOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
