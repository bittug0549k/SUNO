package com.example.theme

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.io.File
import java.io.FileOutputStream

enum class FontPreset(val displayName: String, val description: String) {
    SYSTEM("System Default", "Native Android typography"),
    MODERN_SANS("Modern Sans", "Clean, futuristic sans-serif"),
    POPPINS("Geometric Grotesk", "Modern rounded geometric typeface"),
    MONOSPACE("Digital Monospace", "Terminal / coding aesthetic"),
    SERIF("Classic Serif", "Elegant literary serif typography"),
    CUSTOM("Custom Imported", "Imported .ttf / .otf from your device");

    companion object {
        fun fromKey(key: String): FontPreset {
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}

object FontEngine {

    fun getFontFamily(fontPreset: FontPreset, customFontPath: String): FontFamily {
        if (fontPreset == FontPreset.CUSTOM && customFontPath.isNotEmpty()) {
            val file = File(customFontPath)
            if (file.exists() && file.canRead()) {
                try {
                    return FontFamily(
                        Font(file = file, weight = FontWeight.Normal),
                        Font(file = file, weight = FontWeight.Medium),
                        Font(file = file, weight = FontWeight.Bold)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return when (fontPreset) {
            FontPreset.SYSTEM -> FontFamily.Default
            FontPreset.MODERN_SANS -> FontFamily.SansSerif
            FontPreset.POPPINS -> FontFamily.SansSerif
            FontPreset.MONOSPACE -> FontFamily.Monospace
            FontPreset.SERIF -> FontFamily.Serif
            FontPreset.CUSTOM -> FontFamily.Default
        }
    }

    /**
     * Imports a user-selected .ttf or .otf file from Uri into app internal secure storage
     */
    fun importFontFromUri(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val fontsDir = File(context.filesDir, "fonts")
            if (!fontsDir.exists()) fontsDir.mkdirs()

            // Resolve file name
            var fileName = "custom_font_${System.currentTimeMillis()}.ttf"
            val pathSegment = uri.lastPathSegment
            if (pathSegment != null && (pathSegment.endsWith(".ttf") || pathSegment.endsWith(".otf"))) {
                fileName = pathSegment.substringAfterLast("/")
            }

            val destFile = File(fontsDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile.exists() && destFile.length() > 0) {
                Pair(destFile.absolutePath, fileName)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
