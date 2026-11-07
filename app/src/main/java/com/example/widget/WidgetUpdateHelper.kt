package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.model.Song
import com.example.service.SunoPlaybackService

object WidgetUpdateHelper {

    fun updateAllWidgets(
        context: Context,
        currentSong: Song?,
        isPlaying: Boolean,
        positionMs: Long = 0,
        durationMs: Long = 1
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // 1. Compact Widget
        val compactComponent = ComponentName(context, SunoCompactWidgetReceiver::class.java)
        val compactIds = appWidgetManager.getAppWidgetIds(compactComponent)
        if (compactIds.isNotEmpty()) {
            for (widgetId in compactIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_suno_compact)

                // Title & Artist
                views.setTextViewText(R.id.widget_track_title, currentSong?.title ?: "SUNO Player")
                views.setTextViewText(R.id.widget_track_artist, currentSong?.artist ?: "Ready to play")

                // Play / Pause Icon
                val playPauseIcon = if (isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                }
                views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

                // Click Intent for Open App
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_compact_root, openAppPendingIntent)

                // Controls PendingIntents
                views.setOnClickPendingIntent(
                    R.id.widget_btn_prev,
                    createServiceActionIntent(context, SunoPlaybackService.ACTION_PREV, 101)
                )
                views.setOnClickPendingIntent(
                    R.id.widget_btn_play_pause,
                    createServiceActionIntent(context, SunoPlaybackService.ACTION_PLAY_PAUSE, 102)
                )
                views.setOnClickPendingIntent(
                    R.id.widget_btn_next,
                    createServiceActionIntent(context, SunoPlaybackService.ACTION_NEXT, 103)
                )

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        // 2. Full Widget
        val fullComponent = ComponentName(context, SunoFullWidgetReceiver::class.java)
        val fullIds = appWidgetManager.getAppWidgetIds(fullComponent)
        if (fullIds.isNotEmpty()) {
            for (widgetId in fullIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_suno_full)

                views.setTextViewText(R.id.widget_full_title, currentSong?.title ?: "SUNO Player")
                views.setTextViewText(R.id.widget_full_artist, currentSong?.artist ?: "Offline Music")

                val progress = if (durationMs > 0) ((positionMs * 100) / durationMs).toInt() else 0
                views.setProgressBar(R.id.widget_full_progress, 100, progress, false)

                val playPauseIcon = if (isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                }
                views.setImageViewResource(R.id.widget_full_btn_play_pause, playPauseIcon)

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_full_root, openAppPendingIntent)

                views.setOnClickPendingIntent(
                    R.id.widget_full_btn_prev,
                    createServiceActionIntent(context, SunoPlaybackService.ACTION_PREV, 201)
                )
                views.setOnClickPendingIntent(
                    R.id.widget_full_btn_play_pause,
                    createServiceActionIntent(context, SunoPlaybackService.ACTION_PLAY_PAUSE, 202)
                )
                views.setOnClickPendingIntent(
                    R.id.widget_full_btn_next,
                    createServiceActionIntent(context, SunoPlaybackService.ACTION_NEXT, 203)
                )

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    private fun createServiceActionIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, SunoPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
