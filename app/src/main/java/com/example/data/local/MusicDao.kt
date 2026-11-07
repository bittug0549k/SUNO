package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.CustomFolderEntity
import com.example.data.local.entity.ExcludedFolderEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SongPlayCountEntity
import com.example.data.local.entity.TagOverrideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Favorites
    @Query("SELECT songId FROM favorites")
    fun getAllFavoriteIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getSongIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    // Tag Overrides
    @Query("SELECT * FROM tag_overrides")
    fun getAllTagOverrides(): Flow<List<TagOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTagOverride(override: TagOverrideEntity)

    // Playback History & Stats
    @Insert
    suspend fun insertPlaybackHistory(history: PlaybackHistoryEntity)

    @Query("SELECT * FROM playback_history WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getPlaybackHistorySince(sinceTimestamp: Long): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT COALESCE(SUM(durationPlayedMs), 0) FROM playback_history WHERE timestamp >= :sinceTimestamp")
    suspend fun getTotalDurationPlayedSince(sinceTimestamp: Long): Long

    @Query("SELECT * FROM song_play_counts ORDER BY playCount DESC")
    fun getTopPlayCounts(): Flow<List<SongPlayCountEntity>>

    @Query("SELECT * FROM song_play_counts ORDER BY lastPlayed DESC")
    fun getRecentlyPlayedCounts(): Flow<List<SongPlayCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayCount(entity: SongPlayCountEntity)

    @Query("SELECT * FROM song_play_counts WHERE songId = :songId")
    suspend fun getPlayCountForSong(songId: Long): SongPlayCountEntity?

    @Transaction
    suspend fun recordSongPlay(songId: Long, playedDurationMs: Long) {
        val existing = getPlayCountForSong(songId)
        val newCount = (existing?.playCount ?: 0) + 1
        upsertPlayCount(
            SongPlayCountEntity(
                songId = songId,
                playCount = newCount,
                lastPlayed = System.currentTimeMillis()
            )
        )
        if (playedDurationMs > 3000) {
            insertPlaybackHistory(
                PlaybackHistoryEntity(
                    songId = songId,
                    durationPlayedMs = playedDurationMs,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Excluded Folders
    @Query("SELECT * FROM excluded_folders")
    fun getAllExcludedFolders(): Flow<List<ExcludedFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcludedFolder(folder: ExcludedFolderEntity)

    @Query("DELETE FROM excluded_folders WHERE folderPath = :folderPath")
    suspend fun deleteExcludedFolder(folderPath: String)

    // Custom Folders
    @Query("SELECT * FROM custom_folders")
    fun getAllCustomFolders(): Flow<List<CustomFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomFolder(folder: CustomFolderEntity)

    @Query("DELETE FROM custom_folders WHERE folderUri = :folderUri")
    suspend fun deleteCustomFolder(folderUri: String)

    // Listening Sessions & Real-time Analytics
    @Insert
    suspend fun insertListeningSession(session: com.example.data.local.entity.ListeningSession): Long

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM listening_sessions WHERE timestamp >= :sinceTimestamp")
    suspend fun getListeningSecondsSince(sinceTimestamp: Long): Long

    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM listening_sessions")
    suspend fun getAllTimeListeningSeconds(): Long

    @Query("SELECT * FROM listening_sessions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getListeningSessionsSince(sinceTimestamp: Long): Flow<List<com.example.data.local.entity.ListeningSession>>

    @Query("SELECT artistId AS artist, SUM(durationSeconds) AS totalSeconds, COUNT(*) AS playCount FROM listening_sessions WHERE artistId != '' GROUP BY artistId ORDER BY totalSeconds DESC LIMIT 10")
    fun getTopArtistsByListening(): Flow<List<com.example.data.local.entity.ArtistStat>>

    // Equalizer Profiles
    @Query("SELECT * FROM equalizer_profiles ORDER BY id ASC")
    fun getAllEqualizerProfiles(): Flow<List<com.example.data.local.entity.EqualizerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEqualizerProfile(profile: com.example.data.local.entity.EqualizerProfileEntity): Long

    @Query("DELETE FROM equalizer_profiles WHERE id = :profileId")
    suspend fun deleteEqualizerProfile(profileId: Long)

    @Query("UPDATE equalizer_profiles SET isSelected = 0")
    suspend fun clearActiveEqualizerProfiles()

    @Query("UPDATE equalizer_profiles SET isSelected = 1 WHERE id = :profileId")
    suspend fun setActiveEqualizerProfile(profileId: Long)

    @Transaction
    suspend fun switchActiveProfile(profileId: Long) {
        clearActiveEqualizerProfiles()
        setActiveEqualizerProfile(profileId)
    }
}
