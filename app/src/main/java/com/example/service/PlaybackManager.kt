package com.example.service

import androidx.compose.ui.graphics.Color
import com.example.model.RepeatMode
import com.example.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackManager {
    val audioEffectEngine = AudioEffectEngine()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow(0)
    val sleepTimerSeconds: StateFlow<Int> = _sleepTimerSeconds.asStateFlow()

    private val _sleepAtTrackEnd = MutableStateFlow(false)
    val sleepAtTrackEnd: StateFlow<Boolean> = _sleepAtTrackEnd.asStateFlow()

    private val _dominantColor = MutableStateFlow(Color(0xFF00E5FF))
    val dominantColor: StateFlow<Color> = _dominantColor.asStateFlow()

    private val _secondaryColor = MutableStateFlow(Color(0xFFFF007F))
    val secondaryColor: StateFlow<Color> = _secondaryColor.asStateFlow()

    // Command callbacks wired to SunoPlaybackService
    var onPlaySongCallback: ((Song, List<Song>) -> Unit)? = null
    var onTogglePlayPauseCallback: (() -> Unit)? = null
    var onSeekCallback: ((Long) -> Unit)? = null
    var onNextCallback: (() -> Unit)? = null
    var onPrevCallback: (() -> Unit)? = null
    var onShuffleToggledCallback: ((Boolean) -> Unit)? = null
    var onRepeatModeChangedCallback: ((RepeatMode) -> Unit)? = null

    fun updateCurrentSong(song: Song?) {
        _currentSong.value = song
        _durationMs.value = song?.durationMs ?: 1L
    }

    fun updatePlaybackState(playing: Boolean, posMs: Long, durMs: Long) {
        _isPlaying.value = playing
        _currentPositionMs.value = posMs
        if (durMs > 0) _durationMs.value = durMs
    }

    fun updateQueue(songs: List<Song>, index: Int) {
        _queue.value = songs
        _currentIndex.value = index
    }

    fun updateDominantColors(dominant: Color, secondary: Color) {
        _dominantColor.value = dominant
        _secondaryColor.value = secondary
    }

    fun setSleepTimer(minutes: Int, atTrackEnd: Boolean = false) {
        _sleepAtTrackEnd.value = atTrackEnd
        _sleepTimerSeconds.value = if (atTrackEnd) 0 else minutes * 60
    }

    fun tickSleepTimer(): Boolean {
        val current = _sleepTimerSeconds.value
        if (current > 0) {
            _sleepTimerSeconds.value = current - 1
            if (_sleepTimerSeconds.value <= 0) {
                return true // Timer triggered!
            }
        }
        return false
    }

    fun cancelSleepTimer() {
        _sleepTimerSeconds.value = 0
        _sleepAtTrackEnd.value = false
    }

    // High level UI actions
    fun playSong(song: Song, newQueue: List<Song> = emptyList()) {
        onPlaySongCallback?.invoke(song, if (newQueue.isNotEmpty()) newQueue else listOf(song))
    }

    fun togglePlayPause() {
        onTogglePlayPauseCallback?.invoke()
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        onSeekCallback?.invoke(positionMs)
    }

    fun playNext() {
        onNextCallback?.invoke()
    }

    fun playPrevious() {
        onPrevCallback?.invoke()
    }

    fun toggleShuffle() {
        val newShuffle = !_shuffleEnabled.value
        _shuffleEnabled.value = newShuffle
        onShuffleToggledCallback?.invoke(newShuffle)
    }

    fun cycleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = newMode
        onRepeatModeChangedCallback?.invoke(newMode)
    }
}
