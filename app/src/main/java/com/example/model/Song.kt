package com.example.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val path: String = "",
    val folder: String = "Internal",
    val year: Int = 0,
    val genre: String = "Unknown",
    val sizeBytes: Long = 0,
    val bitrateKbps: Int = 320,
    val isFavorite: Boolean = false,
    val isDemoTrack: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class LyricsLine(
    val timeMs: Long,
    val text: String
)

enum class RepeatMode {
    OFF, ALL, ONE
}
