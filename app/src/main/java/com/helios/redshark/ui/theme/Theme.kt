package com.helios.redshark.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = RedSharkRed,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = RedSharkRedContainer,
    onPrimaryContainer = OnRedSharkRedContainer,

    secondary = SeaTeal,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SeaTealContainer,
    onSecondaryContainer = OnSeaTealContainer,

    tertiary = Amber,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,

    error = ErrorRed,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = OnErrorRedContainer,

    background = NeutralBackgroundLight,
    onBackground = NeutralOnSurfaceLight,
    surface = NeutralSurfaceLight,
    onSurface = NeutralOnSurfaceLight,
    surfaceVariant = NeutralSurfaceVariantLight,
    onSurfaceVariant = NeutralOnSurfaceVariantLight,
    outline = NeutralOutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = RedSharkRedContainer,
    onPrimary = OnRedSharkRedContainer,
    primaryContainer = RedSharkRedDark,
    onPrimaryContainer = RedSharkRedContainer,

    secondary = SeaTealContainer,
    onSecondary = OnSeaTealContainer,
    secondaryContainer = SeaTealDark,
    onSecondaryContainer = SeaTealContainer,

    tertiary = AmberContainer,
    onTertiary = OnAmberContainer,
    tertiaryContainer = AmberDark,
    onTertiaryContainer = AmberContainer,

    error = ErrorRedContainer,
    onError = OnErrorRedContainer,
    errorContainer = ErrorRedDark,
    onErrorContainer = ErrorRedContainer,

    background = NeutralBackgroundDark,
    onBackground = NeutralOnSurfaceDark,
    surface = NeutralSurfaceDark,
    onSurface = NeutralOnSurfaceDark,
    surfaceVariant = NeutralSurfaceVariantDark,
    onSurfaceVariant = NeutralOnSurfaceVariantDark,
    outline = NeutralOutlineDark,
)

@Composable
fun RedSharkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
