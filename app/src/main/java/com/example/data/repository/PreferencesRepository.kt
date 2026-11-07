package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "suno_preferences")

class PreferencesRepository(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme_preset")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, DARK, LIGHT
        val KEY_ICON_PACK = stringPreferencesKey("icon_pack") // DEFAULT, NEON, DIWALI, HOLI, GOLDEN
        val KEY_FONT_TYPE = stringPreferencesKey("font_preset")
        val KEY_CUSTOM_FONT_PATH = stringPreferencesKey("custom_font_path")
        val KEY_CUSTOM_FONT_NAME = stringPreferencesKey("custom_font_name")
        val KEY_CROSSFADE_SEC = intPreferencesKey("crossfade_sec")
        val KEY_GAPLESS = booleanPreferencesKey("gapless_playback")
        val KEY_REPLAY_GAIN = booleanPreferencesKey("replay_gain_normalize")
        val KEY_FILTER_SHORT_AUDIO = booleanPreferencesKey("filter_short_audio")
        val KEY_EQ_PRESET = stringPreferencesKey("eq_preset_name")
        val KEY_EQ_BANDS = stringPreferencesKey("eq_band_levels")
        val KEY_BASS_BOOST = intPreferencesKey("eq_bass_boost")
        val KEY_VIRTUALIZER = intPreferencesKey("eq_virtualizer")
        val KEY_REVERB = intPreferencesKey("eq_reverb")
        val KEY_CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME] ?: "CYBER_NEON" }
    val themeModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME_MODE] ?: "DARK" }
    val iconPackFlow: Flow<String> = context.dataStore.data.map { it[KEY_ICON_PACK] ?: "DEFAULT" }
    val fontPresetFlow: Flow<String> = context.dataStore.data.map { it[KEY_FONT_TYPE] ?: "SYSTEM" }
    val customFontPathFlow: Flow<String> = context.dataStore.data.map { it[KEY_CUSTOM_FONT_PATH] ?: "" }
    val customFontNameFlow: Flow<String> = context.dataStore.data.map { it[KEY_CUSTOM_FONT_NAME] ?: "" }
    val crossfadeFlow: Flow<Int> = context.dataStore.data.map { it[KEY_CROSSFADE_SEC] ?: 0 }
    val gaplessFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_GAPLESS] ?: true }
    val replayGainFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_REPLAY_GAIN] ?: false }
    val filterShortAudioFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_FILTER_SHORT_AUDIO] ?: true }
    val customWallpaperUriFlow: Flow<String> = context.dataStore.data.map { it[KEY_CUSTOM_WALLPAPER_URI] ?: "" }

    val eqPresetFlow: Flow<String> = context.dataStore.data.map { it[KEY_EQ_PRESET] ?: "SUNO Dynamic" }
    val eqBandsFlow: Flow<String> = context.dataStore.data.map { it[KEY_EQ_BANDS] ?: "3,2,0,1,3" }
    val bassBoostFlow: Flow<Int> = context.dataStore.data.map { it[KEY_BASS_BOOST] ?: 400 }
    val virtualizerFlow: Flow<Int> = context.dataStore.data.map { it[KEY_VIRTUALIZER] ?: 300 }
    val reverbFlow: Flow<Int> = context.dataStore.data.map { it[KEY_REVERB] ?: 0 }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setIconPack(pack: String) {
        context.dataStore.edit { it[KEY_ICON_PACK] = pack }
    }

    suspend fun setFontPreset(fontPreset: String) {
        context.dataStore.edit { it[KEY_FONT_TYPE] = fontPreset }
    }

    suspend fun setCustomFont(path: String, name: String) {
        context.dataStore.edit {
            it[KEY_CUSTOM_FONT_PATH] = path
            it[KEY_CUSTOM_FONT_NAME] = name
            it[KEY_FONT_TYPE] = "CUSTOM"
        }
    }

    suspend fun setCrossfade(seconds: Int) {
        context.dataStore.edit { it[KEY_CROSSFADE_SEC] = seconds }
    }

    suspend fun setGapless(enabled: Boolean) {
        context.dataStore.edit { it[KEY_GAPLESS] = enabled }
    }

    suspend fun setReplayGain(enabled: Boolean) {
        context.dataStore.edit { it[KEY_REPLAY_GAIN] = enabled }
    }

    suspend fun setFilterShortAudio(enabled: Boolean) {
        context.dataStore.edit { it[KEY_FILTER_SHORT_AUDIO] = enabled }
    }

    suspend fun setCustomWallpaperUri(uri: String) {
        context.dataStore.edit { it[KEY_CUSTOM_WALLPAPER_URI] = uri }
    }

    suspend fun saveEqualizerSettings(
        presetName: String,
        bandLevels: String,
        bassBoost: Int,
        virtualizer: Int,
        reverb: Int
    ) {
        context.dataStore.edit {
            it[KEY_EQ_PRESET] = presetName
            it[KEY_EQ_BANDS] = bandLevels
            it[KEY_BASS_BOOST] = bassBoost
            it[KEY_VIRTUALIZER] = virtualizer
            it[KEY_REVERB] = reverb
        }
    }
}
