package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = GharTakEmeraldBright,
    onPrimary = Color.White,
    primaryContainer = GharTakEmeraldDark,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = GtNavyHeader,
    onSecondary = Color.White,
    secondaryContainer = GharTakCharcoalElevated,
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = GharTakEmeraldBright,
    onTertiary = Color.White,
    background = GtNavyDeep,
    onBackground = Color(0xFFF8FAFC),
    surface = GharTakCharcoalSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = GharTakCharcoalElevated,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = GharTakOutlineDark,
    error = GharTakCoralError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GharTakEmerald,
    onPrimary = Color.White,
    primaryContainer = GtMintTint,
    onPrimaryContainer = GharTakEmeraldDark,
    secondary = GtNavyHeader,
    onSecondary = Color.White,
    secondaryContainer = GtLightGreyCard,
    onSecondaryContainer = GtDarkNavyText,
    tertiary = GharTakEmerald,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = GtDarkNavyText,
    surface = Color.White,
    onSurface = GtDarkNavyText,
    surfaceVariant = GtLightGreyCard,
    onSurfaceVariant = GtSlateNavyMuted,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = GtLightGreyCard,
    surfaceContainerHigh = GtLightGreyElevated,
    surfaceContainerHighest = Color(0xFFE2E8F0),
    outline = GharTakOutlineLight,
    error = GharTakCoralError,
    onError = Color.White
)

val GharTakShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gtNavyTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = GtNavyHeader,
    scrolledContainerColor = GtNavyHeader,
    titleContentColor = Color.White,
    navigationIconContentColor = Color.White,
    actionIconContentColor = Color.White
)

@Composable
fun GharTakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = GharTakShapes,
        content = content
    )
}
