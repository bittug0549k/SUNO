package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int = 0
)

@Entity(tableName = "tag_overrides")
data class TagOverrideEntity(
    @PrimaryKey
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val year: Int
)

@Entity(
    tableName = "playback_history",
    indices = [Index(value = ["timestamp"]), Index(value = ["songId"])]
)
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val durationPlayedMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "listening_sessions",
    indices = [Index(value = ["timestamp"]), Index(value = ["trackId"])]
)
data class ListeningSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long,
    val trackId: Long,
    val artistId: String = ""
)

@Entity(tableName = "song_play_counts")
data class SongPlayCountEntity(
    @PrimaryKey
    val songId: Long,
    val playCount: Int = 1,
    val lastPlayed: Long = System.currentTimeMillis()
)

@Entity(tableName = "equalizer_profiles")
data class EqualizerProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bandLevels: String, // comma-separated e.g. "3,2,0,1,3"
    val bassBoost: Int = 400,
    val virtualizer: Int = 300,
    val reverb: Int = 0,
    val isSelected: Boolean = false
)

@Entity(tableName = "excluded_folders")
data class ExcludedFolderEntity(
    @PrimaryKey
    val folderPath: String
)

@Entity(tableName = "custom_folders")
data class CustomFolderEntity(
    @PrimaryKey
    val folderUri: String,
    val folderName: String
)

data class ArtistStat(
    val artist: String,
    val totalSeconds: Long,
    val playCount: Int
)
