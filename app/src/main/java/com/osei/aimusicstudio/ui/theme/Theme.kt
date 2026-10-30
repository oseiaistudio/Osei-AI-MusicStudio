package com.osei.aimusicstudio.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StudioPrimary,
    onPrimary = TextPrimary,
    primaryContainer = StudioPrimaryDark,
    onPrimaryContainer = TextPrimary,
    secondary = StudioSecondary,
    onSecondary = StudioBackground,
    secondaryContainer = StudioSecondaryDark,
    onSecondaryContainer = TextPrimary,
    tertiary = StudioTertiary,
    onTertiary = StudioBackground,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = DarkGray700
)

@Composable
fun OseiAIMusicStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Studio aesthetics: always maintain dark studio theme
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
