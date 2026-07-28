package com.rtiqa.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.rtiqa.core.design.tokens.LocalRdsElevation
import com.rtiqa.core.design.tokens.LocalRdsSpacing
import com.rtiqa.core.design.tokens.RdsColor
import com.rtiqa.core.design.tokens.RdsElevation
import com.rtiqa.core.design.tokens.RdsShapes
import com.rtiqa.core.design.tokens.RdsSpacing
import com.rtiqa.core.design.tokens.RdsTypography

private val LightColorScheme = lightColorScheme(
    primary = RdsColor.Primary600,
    onPrimary = RdsColor.LightSurface,
    primaryContainer = RdsColor.Primary100,
    onPrimaryContainer = RdsColor.Primary900,
    secondary = RdsColor.Emerald600,
    onSecondary = RdsColor.LightSurface,
    secondaryContainer = RdsColor.Emerald100,
    onSecondaryContainer = RdsColor.Emerald700,
    tertiary = RdsColor.AiPurple,
    onTertiary = RdsColor.LightSurface,
    tertiaryContainer = RdsColor.AiGlowLight,
    onTertiaryContainer = RdsColor.AiGlowDark,
    background = RdsColor.LightBackground,
    onBackground = RdsColor.LightOnSurface,
    surface = RdsColor.LightSurface,
    onSurface = RdsColor.LightOnSurface,
    surfaceVariant = RdsColor.LightSurfaceVariant,
    onSurfaceVariant = RdsColor.LightOnSurfaceVariant,
    error = RdsColor.Error,
    onError = RdsColor.LightSurface,
    errorContainer = RdsColor.ErrorContainer,
    onErrorContainer = RdsColor.Error,
    outline = RdsColor.LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = RdsColor.Primary400,
    onPrimary = RdsColor.DarkBackground,
    primaryContainer = RdsColor.Primary800,
    onPrimaryContainer = RdsColor.Primary100,
    secondary = RdsColor.Emerald500,
    onSecondary = RdsColor.DarkBackground,
    secondaryContainer = RdsColor.Emerald700,
    onSecondaryContainer = RdsColor.Emerald100,
    tertiary = RdsColor.AiPurple,
    onTertiary = RdsColor.DarkBackground,
    tertiaryContainer = RdsColor.AiGlowDark,
    onTertiaryContainer = RdsColor.AiGlowLight,
    background = RdsColor.DarkBackground,
    onBackground = RdsColor.DarkOnSurface,
    surface = RdsColor.DarkSurface,
    onSurface = RdsColor.DarkOnSurface,
    surfaceVariant = RdsColor.DarkSurfaceVariant,
    onSurfaceVariant = RdsColor.DarkOnSurfaceVariant,
    error = RdsColor.Error,
    onError = RdsColor.DarkBackground,
    errorContainer = RdsColor.ErrorContainer,
    onErrorContainer = RdsColor.Error,
    outline = RdsColor.DarkOutline
)

@Composable
fun RdsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    spacing: RdsSpacing = RdsSpacing(),
    elevation: RdsElevation = RdsElevation(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalRdsSpacing provides spacing,
        LocalRdsElevation provides elevation
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RdsTypography,
            shapes = RdsShapes,
            content = content
        )
    }
}
