package org.insbaixcamp.listadelacompra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = CoralOnPrimary,
    primaryContainer = CoralContainer,
    onPrimaryContainer = CoralOnContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealContainer,
    onSecondaryContainer = TealOnSecondary,
    tertiary = SunnyTertiary,
    onTertiary = SunnyOnTertiary,
    tertiaryContainer = SunnyContainer,
    onTertiaryContainer = SunnyOnTertiary,
    background = CreamBackground,
    onBackground = CreamOnBackground,
    surface = LightSurface,
    onSurface = CreamOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LightError,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF5EC),
    surfaceContainer = Color(0xFFFFF1E6),
    surfaceContainerHigh = Color(0xFFFFEDE0),
    surfaceContainerHighest = Color(0xFFFFE7D6)
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = CoralOnPrimaryDark,
    primaryContainer = CoralContainerDark,
    onPrimaryContainer = CoralOnContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnSecondaryDark,
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = TealSecondaryDark,
    tertiary = SunnyTertiaryDark,
    onTertiary = SunnyOnTertiaryDark,
    tertiaryContainer = SunnyContainerDark,
    onTertiaryContainer = SunnyTertiaryDark,
    background = WarmDarkBackground,
    onBackground = WarmDarkOnBackground,
    surface = DarkSurface,
    onSurface = WarmDarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = DarkError,
    surfaceContainerLowest = Color(0xFF120D0A),
    surfaceContainerLow = Color(0xFF1F1814),
    surfaceContainer = Color(0xFF241D18),
    surfaceContainerHigh = Color(0xFF2E2620),
    surfaceContainerHighest = Color(0xFF38302A)
)

// Formas muy redondeadas para un aspecto amable
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ListaDeLaCompraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
