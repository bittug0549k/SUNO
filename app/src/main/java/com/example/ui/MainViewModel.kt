package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SunoApplication
import com.example.data.local.entity.CustomFolderEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.model.LyricsLine
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.service.PlaybackManager
import com.example.theme.FontEngine
import com.example.theme.FontPreset
import com.example.theme.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SunoApplication
    private val musicRepo = app.musicRepository
    private val prefsRepo = app.preferencesRepository

    // Search query & active tab
    val searchQuery = MutableStateFlow("")
    val activeTab = MutableStateFlow(0) // 0: Home, 1: Playlists, 2: Folders, 3: Albums, 4: Artists, 5: Favorites, 6: Genres, 7: Search

    // Short audio filter preference
    val filterShortAudio: StateFlow<Boolean> = prefsRepo.filterShortAudioFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Songs stream combining MediaStore, demo tracks, exclusions, and duration filter
    val allSongs: StateFlow<List<Song>> = filterShortAudio.flatMapLatest { filterShort ->
        musicRepo.getSongsStream(filterShort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered songs by search
    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, searchQuery) { songs, query ->
        if (query.isBlank()) songs
        else songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true) ||
            it.folder.contains(query, ignoreCase = true) ||
            it.genre.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mixed Carousel Playlist (Auto-shuffled on load or refresh)
    private val _mixedSongs = MutableStateFlow<List<Song>>(emptyList())
    val mixedSongs: StateFlow<List<Song>> = _mixedSongs.asStateFlow()

    fun refreshMixedSongs() {
        _mixedSongs.value = allSongs.value.shuffled()
    }

    // Recent Listen Songs
    val recentListenSongs: StateFlow<List<Song>> = combine(allSongs, musicRepo.getRecentlyPlayedSongIds()) { songs, recentIds ->
        val songMap = songs.associateBy { it.id }
        val recentList = recentIds.mapNotNull { songMap[it] }
        if (recentList.isNotEmpty()) recentList else songs.take(8)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Top Listen Songs
    val topListenSongs: StateFlow<List<Song>> = combine(allSongs, musicRepo.getTopPlayedSongIds()) { songs, topIds ->
        val songMap = songs.associateBy { it.id }
        val topList = topIds.mapNotNull { songMap[it] }
        if (topList.isNotEmpty()) topList else songs.takeLast(8).reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Available Folders across all songs
    val availableFolders: StateFlow<List<String>> = allSongs.combine(MutableStateFlow(Unit)) { songs, _ ->
        songs.map { it.folder }.distinct().filter { it.isNotBlank() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Excluded Folders & Custom Folders
    val excludedFolders: StateFlow<List<String>> = musicRepo.excludedFoldersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customFolders: StateFlow<List<CustomFolderEntity>> = musicRepo.customFoldersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playlists
    val playlists: StateFlow<List<PlaylistEntity>> = musicRepo.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playback state forwarded from PlaybackManager
    val currentSong = PlaybackManager.currentSong
    val isPlaying = PlaybackManager.isPlaying
    val currentPositionMs = PlaybackManager.currentPositionMs
    val durationMs = PlaybackManager.durationMs
    val shuffleEnabled = PlaybackManager.shuffleEnabled
    val repeatMode = PlaybackManager.repeatMode
    val sleepTimerSeconds = PlaybackManager.sleepTimerSeconds
    val sleepAtTrackEnd = PlaybackManager.sleepAtTrackEnd
    val dominantColor = PlaybackManager.dominantColor
    val secondaryColor = PlaybackManager.secondaryColor

    // Lyrics for current song
    val currentLyrics: StateFlow<List<LyricsLine>> = combine(currentSong) { (song) ->
        if (song != null) musicRepo.getLyricsForSong(song) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Appearance & Font settings
    private val _themePreset = MutableStateFlow(ThemePreset.CYBER_NEON)
    val themePreset: StateFlow<ThemePreset> = _themePreset.asStateFlow()

    val themeMode: StateFlow<String> = prefsRepo.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DARK")

    val iconPack: StateFlow<String> = prefsRepo.iconPackFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DEFAULT")

    private val _fontPreset = MutableStateFlow(FontPreset.SYSTEM)
    val fontPreset: StateFlow<FontPreset> = _fontPreset.asStateFlow()

    private val _customFontPath = MutableStateFlow("")
    val customFontPath: StateFlow<String> = _customFontPath.asStateFlow()

    private val _customFontName = MutableStateFlow("")
    val customFontName: StateFlow<String> = _customFontName.asStateFlow()

    val customWallpaperUri: StateFlow<String> = prefsRepo.customWallpaperUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Audio preferences
    val crossfadeSeconds = prefsRepo.crossfadeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val gaplessPlayback = prefsRepo.gaplessFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val replayGain = prefsRepo.replayGainFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Equalizer State
    val eqPresetName = MutableStateFlow("SUNO Dynamic")
    val eqBands = MutableStateFlow(listOf(3, 2, 0, 1, 3))
    val bassBoost = MutableStateFlow(400)
    val virtualizer = MutableStateFlow(300)
    val reverbPreset = MutableStateFlow(0)

    // Playtime Analytics
    val todayMinutes = MutableStateFlow(0L)
    val weekMinutes = MutableStateFlow(0L)
    val monthMinutes = MutableStateFlow(0L)
    val allTimeMinutes = MutableStateFlow(0L)
    val dailyHistory = musicRepo.getDailyListeningHistory(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val topArtists = musicRepo.topArtistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect Theme from datastore
        viewModelScope.launch {
            prefsRepo.themeFlow.collect { key ->
                _themePreset.value = ThemePreset.fromKey(key)
            }
        }
        viewModelScope.launch {
            prefsRepo.fontPresetFlow.collect { key ->
                _fontPreset.value = FontPreset.fromKey(key)
            }
        }
        viewModelScope.launch {
            prefsRepo.customFontPathFlow.collect { path ->
                _customFontPath.value = path
            }
        }
        viewModelScope.launch {
            prefsRepo.customFontNameFlow.collect { name ->
                _customFontName.value = name
            }
        }
        viewModelScope.launch {
            prefsRepo.eqPresetFlow.collect { name ->
                eqPresetName.value = name
            }
        }
        viewModelScope.launch {
            prefsRepo.bassBoostFlow.collect { bass ->
                bassBoost.value = bass
                PlaybackManager.audioEffectEngine.setBassBoost(bass.toShort())
            }
        }
        viewModelScope.launch {
            prefsRepo.virtualizerFlow.collect { virt ->
                virtualizer.value = virt
                PlaybackManager.audioEffectEngine.setVirtualizer(virt.toShort())
            }
        }
        viewModelScope.launch {
            prefsRepo.reverbFlow.collect { rev ->
                reverbPreset.value = rev
                PlaybackManager.audioEffectEngine.setReverbPreset(rev.toShort())
            }
        }
        // Initialize mixed songs once loaded
        viewModelScope.launch {
            allSongs.collect { songs ->
                if (_mixedSongs.value.isEmpty() && songs.isNotEmpty()) {
                    _mixedSongs.value = songs.shuffled()
                }
            }
        }

        refreshPlaytimeAnalytics()
    }

    fun refreshPlaytimeAnalytics() {
        viewModelScope.launch {
            todayMinutes.value = musicRepo.getTodayPlaytimeMinutes()
            weekMinutes.value = musicRepo.getWeekPlaytimeMinutes()
            monthMinutes.value = musicRepo.getMonthPlaytimeMinutes()
            allTimeMinutes.value = musicRepo.getAllTimePlaytimeMinutes()
        }
    }

    // Playback actions
    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val targetQueue = if (queue.isNotEmpty()) queue else allSongs.value
        PlaybackManager.playSong(song, targetQueue)
        refreshPlaytimeAnalytics()
    }

    fun togglePlayPause() {
        if (currentSong.value == null && allSongs.value.isNotEmpty()) {
            playSong(allSongs.value.first())
        } else {
            PlaybackManager.togglePlayPause()
        }
    }

    fun seekTo(positionMs: Long) {
        PlaybackManager.seekTo(positionMs)
    }

    fun playNext() {
        PlaybackManager.playNext()
    }

    fun playPrevious() {
        PlaybackManager.playPrevious()
    }

    fun toggleShuffle() {
        PlaybackManager.toggleShuffle()
    }

    fun enableShuffle(enabled: Boolean) {
        if (PlaybackManager.shuffleEnabled.value != enabled) {
            PlaybackManager.toggleShuffle()
        }
    }

    fun cycleRepeatMode() {
        PlaybackManager.cycleRepeatMode()
    }

    fun setCustomWallpaperUri(uri: String) {
        viewModelScope.launch {
            prefsRepo.setCustomWallpaperUri(uri)
        }
    }

    fun clearCustomWallpaper() {
        viewModelScope.launch {
            prefsRepo.setCustomWallpaperUri("")
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepo.toggleFavorite(song.id, song.isFavorite)
        }
    }

    // Appearance actions
    fun setTheme(preset: ThemePreset) {
        _themePreset.value = preset
        viewModelScope.launch {
            prefsRepo.setTheme(preset.name)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefsRepo.setThemeMode(mode)
        }
    }

    fun setIconPack(pack: String) {
        viewModelScope.launch {
            prefsRepo.setIconPack(pack)
        }
    }

    fun setFontPreset(preset: FontPreset) {
        _fontPreset.value = preset
        viewModelScope.launch {
            prefsRepo.setFontPreset(preset.name)
        }
    }

    fun importCustomFont(uri: Uri) {
        viewModelScope.launch {
            val result = FontEngine.importFontFromUri(getApplication(), uri)
            if (result != null) {
                _customFontPath.value = result.first
                _customFontName.value = result.second
                _fontPreset.value = FontPreset.CUSTOM
                prefsRepo.setCustomFont(result.first, result.second)
            }
        }
    }

    // Settings actions
    fun setCrossfade(seconds: Int) {
        viewModelScope.launch { prefsRepo.setCrossfade(seconds) }
    }

    fun setGapless(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setGapless(enabled) }
    }

    fun setReplayGain(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setReplayGain(enabled) }
    }

    fun setFilterShortAudio(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setFilterShortAudio(enabled) }
    }

    // Excluded Folders
    fun addExcludedFolder(folderPath: String) {
        viewModelScope.launch { musicRepo.addExcludedFolder(folderPath) }
    }

    fun removeExcludedFolder(folderPath: String) {
        viewModelScope.launch { musicRepo.removeExcludedFolder(folderPath) }
    }

    // Custom Folders (SAF)
    fun addCustomFolder(uri: String, name: String) {
        viewModelScope.launch { musicRepo.addCustomFolder(uri, name) }
    }

    fun removeCustomFolder(uri: String) {
        viewModelScope.launch { musicRepo.removeCustomFolder(uri) }
    }

    // Equalizer actions
    fun setBandLevel(bandIndex: Int, level: Int) {
        val currentList = eqBands.value.toMutableList()
        if (bandIndex in currentList.indices) {
            currentList[bandIndex] = level
            eqBands.value = currentList
            eqPresetName.value = "Custom"
            PlaybackManager.audioEffectEngine.setBandLevel(bandIndex.toShort(), (level * 100).toShort())
            saveEqualizer()
        }
    }

    fun setBassBoostLevel(level: Int) {
        bassBoost.value = level
        PlaybackManager.audioEffectEngine.setBassBoost(level.toShort())
        saveEqualizer()
    }

    fun setVirtualizerLevel(level: Int) {
        virtualizer.value = level
        PlaybackManager.audioEffectEngine.setVirtualizer(level.toShort())
        saveEqualizer()
    }

    fun setReverb(preset: Int) {
        reverbPreset.value = preset
        PlaybackManager.audioEffectEngine.setReverbPreset(preset.toShort())
        saveEqualizer()
    }

    fun applyEqPreset(name: String, bands: List<Int>, bass: Int = 400, virt: Int = 300) {
        eqPresetName.value = name
        eqBands.value = bands
        bassBoost.value = bass
        virtualizer.value = virt
        bands.forEachIndexed { index, level ->
            PlaybackManager.audioEffectEngine.setBandLevel(index.toShort(), (level * 100).toShort())
        }
        PlaybackManager.audioEffectEngine.setBassBoost(bass.toShort())
        PlaybackManager.audioEffectEngine.setVirtualizer(virt.toShort())
        saveEqualizer()
    }

    private fun saveEqualizer() {
        viewModelScope.launch {
            prefsRepo.saveEqualizerSettings(
                presetName = eqPresetName.value,
                bandLevels = eqBands.value.joinToString(","),
                bassBoost = bassBoost.value,
                virtualizer = virtualizer.value,
                reverb = reverbPreset.value
            )
        }
    }

    // Sleep Timer actions
    fun setSleepTimer(minutes: Int, atTrackEnd: Boolean = false) {
        PlaybackManager.setSleepTimer(minutes, atTrackEnd)
    }

    fun cancelSleepTimer() {
        PlaybackManager.cancelSleepTimer()
    }

    // Playlists
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepo.createPlaylist(name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            musicRepo.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepo.addSongToPlaylist(playlistId, songId)
        }
    }

    // Tag Editor
    fun updateSongTags(songId: Long, title: String, artist: String, album: String, year: Int) {
        viewModelScope.launch {
            musicRepo.updateSongTags(songId, title, artist, album, year)
        }
    }
}
