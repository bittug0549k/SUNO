package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.palette.graphics.Palette
import com.example.SunoApplication
import com.example.data.local.entity.ListeningSession
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SunoPlaybackService : MediaSessionService() {

    companion object {
        const val ACTION_PLAY = "com.example.suno.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.suno.ACTION_PAUSE"
        const val ACTION_PLAY_PAUSE = "com.example.suno.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.suno.ACTION_NEXT"
        const val ACTION_PREV = "com.example.suno.ACTION_PREV"
    }

    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null
    private lateinit var notificationHelper: MediaNotificationHelper

    // SupervisorJob ensures background errors in child tasks don't crash playback service
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + serviceJob)
    private var progressTickerJob: Job? = null
    private var cachedAlbumArtBitmap: Bitmap? = null
    private var lastRecordedSongId: Long = -1L

    // Accurate listening duration tracking
    private var accumulatedListeningSec: Long = 0L
    private var activeSongForSession: Song? = null

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (player.isPlaying) {
                    player.pause()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = MediaNotificationHelper(this)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        mediaSession = MediaSession.Builder(this, player).build()

        // Immediate startForeground to guarantee Android 14+ background compliance
        try {
            val initialNotification = notificationHelper.buildNotification(null, false, null, mediaSession)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    MediaNotificationHelper.NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(MediaNotificationHelper.NOTIFICATION_ID, initialNotification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        registerReceiver(
            noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )

        // Attach initial audio session ID
        if (player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            PlaybackManager.audioEffectEngine.attachSession(player.audioSessionId)
        }

        setupPlayerListeners()
        wirePlaybackManager()
        startProgressTicker()
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                updateNotification()
                if (!isPlaying) {
                    flushListeningSession()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                flushListeningSession()

                val songId = mediaItem?.mediaId?.toLongOrNull()
                val queue = PlaybackManager.queue.value
                val newSong = queue.firstOrNull { it.id == songId } ?: queue.getOrNull(player.currentMediaItemIndex)
                if (newSong != null) {
                    activeSongForSession = newSong
                    accumulatedListeningSec = 0L

                    PlaybackManager.updateCurrentSong(newSong)
                    PlaybackManager.updateQueue(queue, player.currentMediaItemIndex)
                    loadArtworkAndPalette(newSong)
                    updateNotification()

                    if (newSong.id != lastRecordedSongId) {
                        lastRecordedSongId = newSong.id
                        serviceScope.launch(Dispatchers.IO) {
                            try {
                                (application as? SunoApplication)?.musicRepository?.recordPlayback(newSong.id, 5000L)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
                if (playbackState == Player.STATE_ENDED) {
                    flushListeningSession()
                    if (PlaybackManager.sleepAtTrackEnd.value) {
                        player.pause()
                        PlaybackManager.cancelSleepTimer()
                    } else if (PlaybackManager.repeatMode.value == RepeatMode.ALL && player.mediaItemCount > 0) {
                        player.seekToDefaultPosition(0)
                        player.play()
                    } else if (PlaybackManager.repeatMode.value == RepeatMode.ONE) {
                        player.seekTo(0)
                        player.play()
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    PlaybackManager.audioEffectEngine.attachSession(audioSessionId)
                }
            }
        })
    }

    private fun flushListeningSession() {
        val currentTrack = activeSongForSession ?: PlaybackManager.currentSong.value
        val duration = accumulatedListeningSec
        if (currentTrack != null && duration >= 3) {
            accumulatedListeningSec = 0L
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val dao = (application as? SunoApplication)?.database?.musicDao()
                    dao?.insertListeningSession(
                        ListeningSession(
                            timestamp = System.currentTimeMillis(),
                            durationSeconds = duration,
                            trackId = currentTrack.id,
                            artistId = currentTrack.artist
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            accumulatedListeningSec = 0L
        }
    }

    private fun wirePlaybackManager() {
        PlaybackManager.onPlaySongCallback = { song, queue ->
            playSongInternal(song, queue)
        }

        PlaybackManager.onTogglePlayPauseCallback = {
            if (player.isPlaying) {
                player.pause()
            } else {
                if (player.playbackState == Player.STATE_IDLE && PlaybackManager.currentSong.value != null) {
                    playSongInternal(PlaybackManager.currentSong.value!!, PlaybackManager.queue.value)
                } else {
                    player.play()
                }
            }
        }

        PlaybackManager.onSeekCallback = { posMs ->
            player.seekTo(posMs)
        }

        PlaybackManager.onNextCallback = {
            playNextTrack()
        }

        PlaybackManager.onPrevCallback = {
            playPrevTrack()
        }

        PlaybackManager.onShuffleToggledCallback = { enabled ->
            player.shuffleModeEnabled = enabled
        }

        PlaybackManager.onRepeatModeChangedCallback = { mode ->
            player.repeatMode = when (mode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
        }
    }

    private fun playSongInternal(song: Song, queue: List<Song>) {
        flushListeningSession()
        val targetQueue = if (queue.isNotEmpty()) queue else listOf(song)
        val index = targetQueue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }

        activeSongForSession = song
        accumulatedListeningSec = 0L

        PlaybackManager.updateCurrentSong(song)
        PlaybackManager.updateQueue(targetQueue, index)

        try {
            val mediaItems = targetQueue.map { s ->
                MediaItem.Builder()
                    .setMediaId(s.id.toString())
                    .setUri(s.contentUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(s.title)
                            .setArtist(s.artist)
                            .setAlbumTitle(s.album)
                            .build()
                    )
                    .build()
            }

            player.setMediaItems(mediaItems, index, 0L)
            player.shuffleModeEnabled = PlaybackManager.shuffleEnabled.value
            player.repeatMode = when (PlaybackManager.repeatMode.value) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }
            player.prepare()
            player.volume = 1.0f
            player.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        loadArtworkAndPalette(song)
        updateNotification()
    }

    private fun playNextTrack() {
        if (PlaybackManager.repeatMode.value == RepeatMode.ONE) {
            if (player.mediaItemCount > 1) {
                val nextIdx = (player.currentMediaItemIndex + 1) % player.mediaItemCount
                player.seekToDefaultPosition(nextIdx)
                player.play()
            } else {
                player.seekTo(0)
                player.play()
            }
            return
        }

        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (player.mediaItemCount > 0 && (player.repeatMode == Player.REPEAT_MODE_ALL || PlaybackManager.repeatMode.value == RepeatMode.ALL)) {
            player.seekToDefaultPosition(0)
            player.play()
        }
    }

    private fun playPrevTrack() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
            return
        }
        if (PlaybackManager.repeatMode.value == RepeatMode.ONE) {
            player.seekTo(0)
            player.play()
            return
        }

        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else if (player.mediaItemCount > 0 && (player.repeatMode == Player.REPEAT_MODE_ALL || PlaybackManager.repeatMode.value == RepeatMode.ALL)) {
            player.seekToDefaultPosition(player.mediaItemCount - 1)
            player.play()
        }
    }

    private fun loadArtworkAndPalette(song: Song) {
        serviceScope.launch(Dispatchers.IO) {
            var bitmap: Bitmap? = null
            if (song.albumArtUri != null) {
                try {
                    contentResolver.openInputStream(song.albumArtUri)?.use { input ->
                        bitmap = BitmapFactory.decodeStream(input)
                    }
                } catch (ignored: Exception) {}
            }

            cachedAlbumArtBitmap = bitmap

            if (bitmap != null) {
                try {
                    val palette = Palette.from(bitmap!!).generate()
                    val dominant = palette.getDominantColor(0xFF00E5FF.toInt())
                    val vibrant = palette.getVibrantColor(0xFFFF007F.toInt())
                    withContext(Dispatchers.Main) {
                        PlaybackManager.updateDominantColors(Color(dominant), Color(vibrant))
                    }
                } catch (ignored: Exception) {}
            } else {
                val colors = listOf(
                    Pair(Color(0xFF00E5FF), Color(0xFFFF007F)),
                    Pair(Color(0xFFFFD700), Color(0xFFFF5E7E)),
                    Pair(Color(0xFF06D6A0), Color(0xFF118AB2)),
                    Pair(Color(0xFF9D4EDD), Color(0xFFFF70A6)),
                    Pair(Color(0xFFF77F00), Color(0xFFD62828))
                )
                val pair = colors[Math.abs(song.title.hashCode()) % colors.size]
                withContext(Dispatchers.Main) {
                    PlaybackManager.updateDominantColors(pair.first, pair.second)
                }
            }

            withContext(Dispatchers.Main) {
                updateNotification()
            }
        }
    }

    private fun updateState() {
        val duration = if (player.duration > 0) player.duration else PlaybackManager.currentSong.value?.durationMs ?: 1L
        PlaybackManager.updatePlaybackState(
            playing = player.isPlaying,
            posMs = player.currentPosition,
            durMs = duration
        )

        WidgetUpdateHelper.updateAllWidgets(
            this,
            PlaybackManager.currentSong.value,
            player.isPlaying,
            player.currentPosition,
            duration
        )
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    updateState()
                    accumulatedListeningSec += 1L
                    // Flush every 15 seconds of active playback
                    if (accumulatedListeningSec >= 15L) {
                        flushListeningSession()
                    }
                }

                // Check sleep timer
                val remainingSec = PlaybackManager.sleepTimerSeconds.value
                if (remainingSec in 1..5 && player.isPlaying) {
                    val targetVol = (remainingSec - 1).toFloat() / 5.0f
                    player.volume = targetVol.coerceIn(0.0f, 1.0f)
                }

                val timerFired = PlaybackManager.tickSleepTimer()
                if (timerFired) {
                    if (player.isPlaying) {
                        player.pause()
                        player.volume = 1.0f
                    }
                }

                delay(1000)
            }
        }
    }

    private fun updateNotification() {
        val notification = notificationHelper.buildNotification(
            PlaybackManager.currentSong.value,
            player.isPlaying,
            cachedAlbumArtBitmap,
            mediaSession
        )
        if (player.isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    MediaNotificationHelper.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(MediaNotificationHelper.NOTIFICATION_ID, notification)
            }
        } else {
            stopForeground(false)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(MediaNotificationHelper.NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> player.play()
            ACTION_PAUSE -> player.pause()
            ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) player.pause() else player.play()
            }
            ACTION_NEXT -> playNextTrack()
            ACTION_PREV -> playPrevTrack()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        flushListeningSession()
        progressTickerJob?.cancel()
        serviceJob.cancel()
        try { unregisterReceiver(noisyReceiver) } catch (ignored: Exception) {}
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        PlaybackManager.audioEffectEngine.release()
        super.onDestroy()
    }
}
