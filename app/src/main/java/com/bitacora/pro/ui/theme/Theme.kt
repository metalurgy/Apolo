package com.bitacora.pro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00695C),
    onPrimaryContainer = Color(0xFF80CBC4),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00897B),
    onSecondaryContainer = Color(0xFFB2EBE7),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF00695C),
    onTertiaryContainer = Color(0xFFB2EBE7),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFF9C3D54),
    onErrorContainer = Color(0xFFFFB8C8),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1F1F1F),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC7D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454E),
    scrim = Color.Black,
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color(0xFF1F1F1F),
    inversePrimary = Color(0xFF00897B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBE7),
    onPrimaryContainer = Color(0xFF004D48),
    secondary = Color(0xFF4DB6AC),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFB2EBE7),
    onSecondaryContainer = Color(0xFF004D48),
    tertiary = Color(0xFF26A69A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBE7),
    onTertiaryContainer = Color(0xFF004D48),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFE0F2F1),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD),
    scrim = Color.Black,
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFF80CBC4)
)

/**
 * Bitacora Pro theme using Material Design 3.
 */
@Composable
fun BitacoraProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
