package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.theme.ThemePreset

val LocalSunoTheme = staticCompositionLocalOf { ThemePreset.CYBER_NEON }
val LocalSunoFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Default }
val LocalIsDarkTheme = staticCompositionLocalOf { true }

@Composable
fun SunoTheme(
    preset: ThemePreset = ThemePreset.CYBER_NEON,
    themeMode: String = "DARK",
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "SYSTEM" -> isSystemDark
        else -> true
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = preset.primaryColor,
            secondary = preset.secondaryColor,
            tertiary = preset.accentGlow,
            background = preset.backgroundColor,
            surface = preset.surfaceColor,
            surfaceVariant = preset.cardColor,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = preset.onSurface,
            onSurface = preset.onSurface,
            onSurfaceVariant = preset.onSurface.copy(alpha = 0.8f)
        )
    } else {
        lightColorScheme(
            primary = preset.primaryColor,
            secondary = preset.secondaryColor,
            tertiary = preset.accentGlow,
            background = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE2E8F0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF0F172A),
            onSurfaceVariant = Color(0xFF334155)
        )
    }

    val typography = createSunoTypography(fontFamily)

    CompositionLocalProvider(
        LocalSunoTheme provides preset,
        LocalSunoFontFamily provides fontFamily,
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
