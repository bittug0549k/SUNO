package com.example.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemePreset(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardColor: Color,
    val onSurface: Color,
    val accentGlow: Color,
    val borderColor: Color,
    val isGlassmorphism: Boolean = false,
    val accentGradient: Brush = Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF007F))),
    val gradientBrush: Brush? = null
) {
    CYBER_NEON(
        title = "Cyber Neon",
        subtitle = "Dark slate with electric cyan & magenta glow",
        primaryColor = Color(0xFF00F0FF),
        secondaryColor = Color(0xFFFF007F),
        backgroundColor = Color(0xFF0A0A10),
        surfaceColor = Color(0xFF10121F),
        cardColor = Color(0xFF16192E),
        onSurface = Color(0xFFFFFFFF),
        accentGlow = Color(0xFF00F0FF),
        borderColor = Color(0x6600F0FF),
        isGlassmorphism = false,
        accentGradient = Brush.linearGradient(
            listOf(Color(0xFF00F0FF), Color(0xFFFF007F))
        ),
        gradientBrush = Brush.verticalGradient(
            listOf(Color(0xFF0A0A10), Color(0xFF05050A))
        )
    ),

    LUCID_GLASS(
        title = "Lucid Glass",
        subtitle = "Frosted translucent glass aesthetic with ambient glow",
        primaryColor = Color(0xFF64B5F6),
        secondaryColor = Color(0xFFBA68C8),
        backgroundColor = Color(0xFF0C0F1D),
        surfaceColor = Color(0x26FFFFFF),
        cardColor = Color(0x1FFFFFFF),
        onSurface = Color(0xFFF8FAFC),
        accentGlow = Color(0xFF38BDF8),
        borderColor = Color(0x33FFFFFF),
        isGlassmorphism = true,
        accentGradient = Brush.linearGradient(
            listOf(Color(0xFF64B5F6), Color(0xFFBA68C8))
        ),
        gradientBrush = Brush.linearGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0F172A))
        )
    ),

    AMOLED_DARK(
        title = "Minimal AMOLED",
        subtitle = "Zero pixel power #000000 true black battery saver",
        primaryColor = Color(0xFFE2E8F0),
        secondaryColor = Color(0xFF94A3B8),
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF0A0A0A),
        cardColor = Color(0xFF121212),
        onSurface = Color(0xFFFFFFFF),
        accentGlow = Color(0xFFFFFFFF),
        borderColor = Color(0x33FFFFFF),
        isGlassmorphism = false,
        accentGradient = Brush.linearGradient(
            listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
        )
    );

    companion object {
        fun fromKey(key: String): ThemePreset {
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: CYBER_NEON
        }
    }
}

enum class IconPack(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val accentColor: Color
) {
    DEFAULT("Original SUNO", "Electric Cyan & Warm Amber", Color(0xFF00E5FF), Color(0xFFFFD166)),
    CYBER_NEON("Cyber Neon", "Vivid Neon Cyan & Hot Pink", Color(0xFF00F0FF), Color(0xFFFF007F));

    companion object {
        fun fromKey(key: String): IconPack {
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: DEFAULT
        }
    }
}
