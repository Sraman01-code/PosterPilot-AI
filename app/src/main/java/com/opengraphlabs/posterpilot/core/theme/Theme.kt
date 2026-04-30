package com.opengraphlabs.posterpilot.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Parchment,
    primaryContainer = Ink,
    onPrimaryContainer = Parchment,
    secondary = Saffron,
    onSecondary = Ink,
    secondaryContainer = Marigold,
    onSecondaryContainer = SaffronDeep,
    tertiary = Sindoor,
    onTertiary = Bone,
    tertiaryContainer = Sindoor,
    onTertiaryContainer = Bone,
    background = Parchment,
    onBackground = Ink,
    surface = Bone,
    onSurface = Ink,
    surfaceVariant = TeaSoft,
    onSurfaceVariant = InkSoft,
    outline = Tea,
    outlineVariant = TeaSoft
)

private val DarkColorScheme = darkColorScheme(
    primary = Cream,
    onPrimary = Onyx,
    primaryContainer = Slate,
    onPrimaryContainer = Cream,
    secondary = SaffronGlow,
    onSecondary = Onyx,
    secondaryContainer = SaffronDeep,
    onSecondaryContainer = Cream,
    tertiary = Sindoor,
    onTertiary = Cream,
    tertiaryContainer = Sindoor,
    onTertiaryContainer = Cream,
    background = Onyx,
    onBackground = Cream,
    surface = Gunmetal,
    onSurface = Cream,
    surfaceVariant = Slate,
    onSurfaceVariant = CreamSoft,
    outline = Slate,
    outlineVariant = Slate
)

private val PosterPilotShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PosterPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = PosterPilotTypography,
        shapes = PosterPilotShapes,
        content = content
    )
}
