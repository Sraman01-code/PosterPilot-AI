package com.opengraphlabs.posterpilot.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PilotInk,
    onPrimary = Color.White,
    secondary = PilotAmber,
    onSecondary = PilotInk,
    background = PilotSurface,
    onBackground = PilotInk,
    surface = Color.White,
    onSurface = PilotInk
)

@Composable
fun PosterPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = PosterPilotTypography,
        content = content
    )
}
