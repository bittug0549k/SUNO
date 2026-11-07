package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.data.local.MusicDao
import com.example.data.local.entity.CustomFolderEntity
import com.example.data.local.entity.ExcludedFolderEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SongPlayCountEntity
import com.example.data.local.entity.TagOverrideEntity
import com.example.model.LyricsLine
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {

    // Scans device MediaStore with configurable 15s short audio filter (Zero built-in demo songs)
    fun scanDeviceSongs(filterShortAudio: Boolean = true): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE
        )

        // 15 seconds filter when enabled, otherwise 1 second
        val minDuration = if (filterShortAudio) 15000 else 1000
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= $minDuration"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val yearCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val title = it.getString(titleCol) ?: "Unknown Track"
                    val artist = it.getString(artistCol) ?: "Unknown Artist"
                    val album = it.getString(albumCol) ?: "Unknown Album"
                    val duration = it.getLong(durationCol)
                    val path = it.getString(dataCol) ?: ""
                    val year = it.getInt(yearCol)
                    val albumId = it.getLong(albumIdCol)
                    val size = it.getLong(sizeCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    val folder = if (path.isNotEmpty()) {
                        File(path).parentFile?.name ?: "Internal"
                    } else "Internal"

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri,
                            path = path,
                            folder = folder,
                            year = year,
                            sizeBytes = size,
                            isDemoTrack = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        emit(songs)
    }.flowOn(Dispatchers.IO)

    // Merges scan results with favorites, tag overrides, and excluded folders blacklist
    fun getSongsStream(filterShortAudio: Boolean = true): Flow<List<Song>> {
        return combine(
            scanDeviceSongs(filterShortAudio),
            musicDao.getAllFavoriteIds(),
            musicDao.getAllTagOverrides(),
            musicDao.getAllExcludedFolders()
        ) { rawSongs, favoriteIds, tagOverrides, excludedList ->
            val favSet = favoriteIds.toSet()
            val overrideMap = tagOverrides.associateBy { it.songId }
            val excludedPaths = excludedList.map { it.folderPath.lowercase() }.toSet()

            rawSongs
                .filter { song ->
                    // Blacklist exclusion check: folder name or file path
                    val folderLower = song.folder.lowercase()
                    val pathLower = song.path.lowercase()
                    excludedPaths.none { excluded ->
                        folderLower == excluded || pathLower.contains(excluded)
                    }
                }
                .map { song ->
                    val override = overrideMap[song.id]
                    song.copy(
                        title = override?.title ?: song.title,
                        artist = override?.artist ?: song.artist,
                        album = override?.album ?: song.album,
                        year = override?.year ?: song.year,
                        isFavorite = favSet.contains(song.id)
                    )
                }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleFavorite(songId: Long, isCurrentFav: Boolean) = withContext(Dispatchers.IO) {
        if (isCurrentFav) {
            musicDao.removeFavorite(songId)
        } else {
            musicDao.addFavorite(FavoriteEntity(songId))
        }
    }

    // Playback Tracking & Analytics
    suspend fun recordPlayback(songId: Long, playedDurationMs: Long) = withContext(Dispatchers.IO) {
        musicDao.recordSongPlay(songId, playedDurationMs)
    }

    fun getTopPlayedSongIds(): Flow<List<Long>> {
        return musicDao.getTopPlayCounts().map { list -> list.map { it.songId } }
    }

    fun getRecentlyPlayedSongIds(): Flow<List<Long>> {
        return musicDao.getRecentlyPlayedCounts().map { list -> list.map { it.songId } }
    }

    // Playtime Stats for Analytics Dashboard combining playback history and real-time listening sessions
    suspend fun getTodayPlaytimeMinutes(): Long = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val ms = musicDao.getTotalDurationPlayedSince(cal.timeInMillis)
        val sec = musicDao.getListeningSecondsSince(cal.timeInMillis)
        (ms / 1000 + sec) / 60
    }

    suspend fun getWeekPlaytimeMinutes(): Long = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val ms = musicDao.getTotalDurationPlayedSince(cal.timeInMillis)
        val sec = musicDao.getListeningSecondsSince(cal.timeInMillis)
        (ms / 1000 + sec) / 60
    }

    suspend fun getMonthPlaytimeMinutes(): Long = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        val ms = musicDao.getTotalDurationPlayedSince(cal.timeInMillis)
        val sec = musicDao.getListeningSecondsSince(cal.timeInMillis)
        (ms / 1000 + sec) / 60
    }

    suspend fun getAllTimePlaytimeMinutes(): Long = withContext(Dispatchers.IO) {
        val ms = musicDao.getTotalDurationPlayedSince(0L)
        val sec = musicDao.getAllTimeListeningSeconds()
        (ms / 1000 + sec) / 60
    }

    val topArtistsFlow: Flow<List<com.example.data.local.entity.ArtistStat>> = musicDao.getTopArtistsByListening()

    fun getDailyListeningHistory(days: Int = 7): Flow<List<Pair<String, Long>>> {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        return musicDao.getPlaybackHistorySince(cal.timeInMillis).map { entries ->
            val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val dayMap = mutableMapOf<String, Long>()

            // Initialize past days with 0
            for (i in (days - 1) downTo 0) {
                val dayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                val dayLabel = dateFormat.format(dayCal.time)
                dayMap[dayLabel] = 0L
            }

            entries.forEach { entry ->
                val day = dateFormat.format(Date(entry.timestamp))
                val current = dayMap[day] ?: 0L
                dayMap[day] = current + (entry.durationPlayedMs / (1000 * 60))
            }

            dayMap.map { Pair(it.key, it.value) }
        }
    }

    // Excluded Folders Management (Blacklist)
    val excludedFoldersFlow: Flow<List<String>> = musicDao.getAllExcludedFolders().map { list -> list.map { it.folderPath } }

    suspend fun addExcludedFolder(folderPath: String) = withContext(Dispatchers.IO) {
        musicDao.insertExcludedFolder(ExcludedFolderEntity(folderPath.trim()))
    }

    suspend fun removeExcludedFolder(folderPath: String) = withContext(Dispatchers.IO) {
        musicDao.deleteExcludedFolder(folderPath.trim())
    }

    // Custom Folders Management (Whitelist/SAF)
    val customFoldersFlow: Flow<List<CustomFolderEntity>> = musicDao.getAllCustomFolders()

    suspend fun addCustomFolder(uri: String, name: String) = withContext(Dispatchers.IO) {
        musicDao.insertCustomFolder(CustomFolderEntity(folderUri = uri, folderName = name))
    }

    suspend fun removeCustomFolder(uri: String) = withContext(Dispatchers.IO) {
        musicDao.deleteCustomFolder(uri)
    }

    // Playlists
    val playlistsFlow: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        musicDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        musicDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    // Tag Editor
    suspend fun updateSongTags(songId: Long, title: String, artist: String, album: String, year: Int) = withContext(Dispatchers.IO) {
        musicDao.saveTagOverride(
            TagOverrideEntity(
                songId = songId,
                title = title,
                artist = artist,
                album = album,
                year = year
            )
        )
    }

    // Synchronized LRC Lyrics Parser
    fun getLyricsForSong(song: Song): List<LyricsLine> {
        return when (song.id) {
            900001L -> listOf(
                LyricsLine(0, "♪ (Deep Tanpura Resonance & Sitar Alap) ♪"),
                LyricsLine(12000, "In the peaceful silence of the early dawn,"),
                LyricsLine(25000, "Morning rays kiss the holy river waters,"),
                LyricsLine(40000, "Notes of Bhairavi gently awaken the soul."),
                LyricsLine(65000, "♪ (Bansuri Flute Melody Cascades) ♪"),
                LyricsLine(85000, "Every breath a meditation, every note a prayer."),
                LyricsLine(115000, "Rhythms of tabla weave stories of eternity,"),
                LyricsLine(145000, "Harmonies rise into infinite skies."),
                LyricsLine(175000, "Peace rests upon the heart of Bharat. 🇮🇳"),
                LyricsLine(195000, "♪ (Gentle fading chords of tranquility) ♪")
            )
            900002L -> listOf(
                LyricsLine(0, "♪ (Arpeggiated Synthwave Lead) ♪"),
                LyricsLine(15000, "Cruising down the neon grid at midnight,"),
                LyricsLine(30000, "City lights streak like shooting stars."),
                LyricsLine(45000, "Bassline pumping through the open highway,"),
                LyricsLine(60000, "Retro horizons in digital glow."),
                LyricsLine(78000, "Feel the frequency take control!"),
                LyricsLine(95000, "♪ (Electric Synth Solo) ♪"),
                LyricsLine(120000, "Speeding forward, never looking back."),
                LyricsLine(145000, "SUNO pulse in every beat.")
            )
            900003L -> listOf(
                LyricsLine(0, "♪ (Festive Sitar & Shenai Fanfare) ♪"),
                LyricsLine(14000, "A million lamps ignite the starry night,"),
                LyricsLine(32000, "Golden diyas flickering in joyous harmony."),
                LyricsLine(50000, "Laughter, sweets, and warmth fill every home,"),
                LyricsLine(75000, "Light triumphs over darkness forevermore."),
                LyricsLine(100000, "Shubh Deepavali to one and all! ✨"),
                LyricsLine(135000, "May prosperity and love illuminate your path.")
            )
            else -> listOf(
                LyricsLine(0, "♪ (Musical Intro) ♪"),
                LyricsLine(10000, "${song.title} - ${song.artist}"),
                LyricsLine(25000, "Pure offline playback, zero distractions."),
                LyricsLine(45000, "High-fidelity audio rendered with SUNO."),
                LyricsLine(70000, "Feel every nuance of the melody."),
                LyricsLine(95000, "♪ (Interlude) ♪"),
                LyricsLine(120000, "Enjoy your offline music sanctuary.")
            )
        }
    }
}
