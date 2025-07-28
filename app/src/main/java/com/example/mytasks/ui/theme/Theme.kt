package com.example.mytasks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SpaceBlueLight,
    onPrimary = SpaceWhite,
    primaryContainer = SpaceBlue,
    onPrimaryContainer = SpaceWhite,

    secondary = SpacePurpleLight,
    onSecondary = SpaceWhite,
    secondaryContainer = SpacePurple,
    onSecondaryContainer = SpaceWhite,

    tertiary = SpaceAccent,
    onTertiary = SpaceBlack,

    background = SpaceBlack,
    onBackground = SpaceWhite,

    surface = SpaceGray,
    onSurface = SpaceWhite,
    surfaceVariant = SpaceGrayLight,
    onSurfaceVariant = SpaceWhite,

    error = ErrorColor,
    onError = SpaceWhite,

    outline = OutlineColor,
    outlineVariant = OutlineVariantColor
)

private val LightColorScheme = lightColorScheme(
    primary = SpaceBlue,
    onPrimary = SpaceWhite,
    primaryContainer = SpaceBlueLight,
    onPrimaryContainer = SpaceBlack,

    secondary = SpacePurple,
    onSecondary = SpaceWhite,
    secondaryContainer = SpacePurpleLight,
    onSecondaryContainer = SpaceBlack,

    tertiary = SpaceAccent,
    onTertiary = SpaceWhite,

    background = SpaceWhite,
    onBackground = SpaceBlack,

    surface = SpaceWhite,
    onSurface = SpaceBlack,
    surfaceVariant = SpaceGrayLight,
    onSurfaceVariant = SpaceWhite,

    error = ErrorColor,
    onError = SpaceWhite,

    outline = OutlineColor,
    outlineVariant = OutlineVariantColor
)

@Composable
fun MyTasksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}