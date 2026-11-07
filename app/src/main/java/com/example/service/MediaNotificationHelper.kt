package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Song

class MediaNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "suno_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SUNO Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Offline background audio playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        song: Song?,
        isPlaying: Boolean,
        albumArtBitmap: Bitmap? = null,
        mediaSession: androidx.media3.session.MediaSession? = null
    ): Notification {
        val title = song?.title ?: "SUNO Player"
        val artist = song?.artist ?: "Ready to play"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Actions
        val prevPendingIntent = PendingIntent.getService(
            context, 1,
            Intent(context, SunoPlaybackService::class.java).apply { action = SunoPlaybackService.ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPausePendingIntent = PendingIntent.getService(
            context, 2,
            Intent(context, SunoPlaybackService::class.java).apply { action = SunoPlaybackService.ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            context, 3,
            Intent(context, SunoPlaybackService::class.java).apply { action = SunoPlaybackService.ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_suno_logo)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(song?.album ?: "Offline")
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

        if (mediaSession != null) {
            builder.setStyle(
                androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(mediaSession)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }

        if (albumArtBitmap != null) {
            builder.setLargeIcon(albumArtBitmap)
        } else {
            try {
                val defaultArt = BitmapFactory.decodeResource(context.resources, R.drawable.ic_suno_logo)
                builder.setLargeIcon(defaultArt)
            } catch (ignored: Exception) {}
        }

        return builder.build()
    }
}
