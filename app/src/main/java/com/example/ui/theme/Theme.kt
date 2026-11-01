package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = PureWhite,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = PureWhite,
    secondary = SlateDark,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFDAE2FD),
    onSecondaryContainer = Color(0xFF5C647A),
    tertiary = EmeraldPositive,
    onTertiary = PureWhite,
    background = SlateSurface,
    onBackground = SlateDark,
    surface = PureWhite,
    onSurface = SlateDark,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = SlateMedium,
    outline = SlateBorder,
    outlineVariant = SlateLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryLight,
    onPrimary = SlateDark,
    primaryContainer = TealPrimaryDark,
    onPrimaryContainer = PureWhite,
    secondary = Color(0xFFE2E8F0),
    onSecondary = SlateDark,
    tertiary = EmeraldPositive,
    onTertiary = PureWhite,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
