package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ============================================================================
// MediBridge+ Premium Healthcare Design System: Theme Configuration
// ============================================================================

val MediLightColorScheme = lightColorScheme(
    primary = MediNavy,
    onPrimary = MediOnNavy,
    primaryContainer = MediNavyContainer,
    onPrimaryContainer = MediTextPrimary,

    secondary = MediTeal,
    onSecondary = MediOnTeal,
    secondaryContainer = MediTealContainer,
    onSecondaryContainer = MediActionTealText,

    tertiary = MediCyan,
    onTertiary = MediOnNavy,
    tertiaryContainer = MediCyanLight,
    onTertiaryContainer = MediNavy,

    background = MediBackgroundLight,
    onBackground = MediTextPrimary,

    surface = MediSurfaceLight,
    onSurface = MediTextPrimary,

    surfaceVariant = MediSurfaceVariantLight,
    onSurfaceVariant = MediTextSecondary,

    outline = MediInputBorderLight,
    outlineVariant = MediBorderLight,

    error = MediError,
    onError = MediOnError,
    errorContainer = MediErrorBg,
    onErrorContainer = MediErrorText
)

val MediDarkColorScheme = darkColorScheme(
    primary = MediCyanBright,
    onPrimary = MediNavyDark,
    primaryContainer = MediNavyLight,
    onPrimaryContainer = MediTextPrimaryDark,

    secondary = MediTeal,
    onSecondary = MediOnTeal,
    secondaryContainer = MediSurfaceVariantDark,
    onSecondaryContainer = MediTextPrimaryDark,

    tertiary = MediCyan,
    onTertiary = MediNavyDark,
    tertiaryContainer = MediSurfaceVariantDark,
    onTertiaryContainer = MediTextPrimaryDark,

    background = MediBackgroundDark,
    onBackground = MediTextPrimaryDark,

    surface = MediSurfaceDark,
    onSurface = MediTextPrimaryDark,

    surfaceVariant = MediSurfaceVariantDark,
    onSurfaceVariant = MediTextSecondaryDark,

    outline = MediInputBorderDark,
    outlineVariant = MediBorderDark,

    error = MediError,
    onError = MediOnError,
    errorContainer = MediErrorBg,
    onErrorContainer = MediErrorText
)

@Composable
fun MediBridgeTheme(
    darkTheme: Boolean = false, // Enforce clean crisp white theme across entire application
    dynamicColor: Boolean = false, // Keep disabled to strictly enforce MediBridge+ brand identity
    content: @Composable () -> Unit
) {
    // Always use MediLightColorScheme for a pristine white healthcare aesthetic
    MaterialTheme(
        colorScheme = MediLightColorScheme,
        typography = Typography,
        content = content
    )
}
