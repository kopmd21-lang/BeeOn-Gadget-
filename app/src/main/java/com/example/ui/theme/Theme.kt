package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val BeeOnLightColorScheme = lightColorScheme(
    primary = DarazOrange,
    onPrimary = PureWhite,
    primaryContainer = DarazOrangeSoft,
    onPrimaryContainer = DarazOrange,
    secondary = ElectricHoney,
    onSecondary = MidnightNavy,
    secondaryContainer = SoftHoneyTag,
    onSecondaryContainer = DeepAmberText,
    tertiary = WhatsAppEmerald,
    onTertiary = PureWhite,
    tertiaryContainer = Color(0xFFDDF5EB),
    onTertiaryContainer = Color(0xFF0A5438),
    background = MarketplaceCanvas,
    onBackground = MidnightNavy,
    surface = PureWhite,
    onSurface = MidnightNavy,
    surfaceVariant = StudioBackdrop,
    onSurfaceVariant = MutedSlate,
    outline = HairlineSlate,
    outlineVariant = HairlineSlate
)

private val BeeOnDarkColorScheme = darkColorScheme(
    primary = DarazOrangeBright,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF3A1A0B),
    onPrimaryContainer = DarazOrangeBright,
    secondary = ElectricHoney,
    onSecondary = MidnightNavy,
    secondaryContainer = DarkElevated,
    onSecondaryContainer = ElectricHoney,
    tertiary = WhatsAppEmerald,
    onTertiary = PureWhite,
    tertiaryContainer = Color(0xFF0F3C2B),
    onTertiaryContainer = Color(0xFF7BE8BC),
    background = DeepInk,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = SoftSlateText,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorder
)

val BeeOnShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun BeeOnGadgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BeeOnDarkColorScheme else BeeOnLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = BeeOnShapes,
        content = content
    )
}
