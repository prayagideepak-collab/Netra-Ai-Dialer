package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NetraAuthorityEmerald,
    onPrimary = Color(0xFF042F2E),
    primaryContainer = NetraEmeraldContainer,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = NetraRoyalIndigo,
    onSecondary = Color.White,
    tertiary = NetraSkyAccent,
    background = NetraObsidianBg,
    onBackground = Color(0xFFF8FAFC),
    surface = NetraElevatedSlate,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NetraCardSlate,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = NetraCrimsonDanger
)

private val LightColorScheme = lightColorScheme(
    primary = NetraLightSecondary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = NetraLightPrimary,
    onSecondary = Color.White,
    tertiary = Color(0xFF0284C7),
    background = NetraLightBg,
    onBackground = Color(0xFF0F172A),
    surface = NetraLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = NetraLightCard,
    onSurfaceVariant = Color(0xFF334155),
    error = NetraCrimsonDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
