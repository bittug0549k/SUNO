package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpdateInfo(
    val latestVersionCode: Int = 2,
    val latestVersionName: String = "v1.2.0 (Nitro)",
    val releaseNotes: String = "• Native 10-Band DSP Audio Equalizer\n• Ultra-smooth Album Art Swipe Gestures\n• Real-time Listening Stats & Heatmaps\n• Cyber Neon & Lucid Glass Visual Themes\n• Zero-lag Media3 Background Playback",
    val downloadUrl: String = "https://instagram.com/decentbittu07",
    val hasUpdate: Boolean = false
)

object UpdateChecker {
    private val _updateState = MutableStateFlow(
        UpdateInfo(
            latestVersionCode = 2,
            latestVersionName = "v1.2.0 (Nitro)",
            hasUpdate = 2 > BuildConfig.VERSION_CODE
        )
    )
    val updateState: StateFlow<UpdateInfo> = _updateState.asStateFlow()

    fun checkForUpdates() {
        val latestCode = 2
        val currentCode = BuildConfig.VERSION_CODE
        _updateState.value = _updateState.value.copy(
            hasUpdate = latestCode > currentCode
        )
    }

    fun dismissUpdate() {
        _updateState.value = _updateState.value.copy(hasUpdate = false)
    }
}
