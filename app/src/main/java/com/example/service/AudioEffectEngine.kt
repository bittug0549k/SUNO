package com.example.service

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioEffectEngine {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var currentSessionId: Int = 0

    // Cached state to re-apply upon session changes
    private val cachedBandLevels = mutableMapOf<Short, Short>()
    private var cachedBassBoost: Short = 0
    private var cachedVirtualizer: Short = 0
    private var cachedReverb: Short = PresetReverb.PRESET_NONE
    private var isEqEnabled: Boolean = true

    fun attachSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (audioSessionId == currentSessionId && equalizer != null) return
        releaseInternal()
        currentSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEqEnabled
                val numBands = numberOfBands
                for (b in 0 until numBands) {
                    val band = b.toShort()
                    val savedLevel = cachedBandLevels[band]
                    if (savedLevel != null) {
                        setBandLevel(band, savedLevel)
                    } else {
                        cachedBandLevels[band] = getBandLevel(band)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Failed to init Equalizer: ${e.message}")
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(cachedBassBoost)
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Failed to init BassBoost: ${e.message}")
        }

        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
                if (strengthSupported) {
                    setStrength(cachedVirtualizer)
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Failed to init Virtualizer: ${e.message}")
        }

        try {
            presetReverb = PresetReverb(0, audioSessionId).apply {
                enabled = true
                preset = cachedReverb
            }
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Failed to init PresetReverb: ${e.message}")
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        isEqEnabled = enabled
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Error toggling equalizer: ${e.message}")
        }
    }

    fun isEqualizerEnabled(): Boolean = isEqEnabled

    fun getNumberOfBands(): Int {
        return equalizer?.numberOfBands?.toInt() ?: 5
    }

    fun getBandFreqRange(band: Short): Pair<Int, Int> {
        val range = equalizer?.getBandFreqRange(band)
        return if (range != null && range.size >= 2) {
            Pair(range[0] / 1000, range[1] / 1000)
        } else {
            Pair(60, 14000)
        }
    }

    fun getCenterFreq(band: Short): Int {
        return (equalizer?.getCenterFreq(band) ?: 1000000) / 1000
    }

    fun getBandLevelRange(): Pair<Short, Short> {
        val range = equalizer?.bandLevelRange
        return if (range != null && range.size >= 2) {
            Pair(range[0], range[1])
        } else {
            Pair((-1500).toShort(), 1500.toShort())
        }
    }

    fun getBandLevel(band: Short): Short {
        return cachedBandLevels[band] ?: equalizer?.getBandLevel(band) ?: 0
    }

    fun setBandLevel(band: Short, level: Short) {
        cachedBandLevels[band] = level
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Error setting band level: ${e.message}")
        }
    }

    fun getBassBoost(): Short = cachedBassBoost

    fun setBassBoost(strength: Short) {
        val clamped = strength.coerceIn(0, 1000)
        cachedBassBoost = clamped
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(clamped)
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Error setting bass boost: ${e.message}")
        }
    }

    fun getVirtualizer(): Short = cachedVirtualizer

    fun setVirtualizer(strength: Short) {
        val clamped = strength.coerceIn(0, 1000)
        cachedVirtualizer = clamped
        try {
            virtualizer?.let {
                if (it.strengthSupported) {
                    it.setStrength(clamped)
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Error setting virtualizer: ${e.message}")
        }
    }

    fun getReverbPreset(): Short = cachedReverb

    fun setReverbPreset(preset: Short) {
        cachedReverb = preset
        try {
            presetReverb?.preset = preset
        } catch (e: Exception) {
            Log.w("AudioEffectEngine", "Error setting reverb: ${e.message}")
        }
    }

    private fun releaseInternal() {
        try { equalizer?.release() } catch (ignored: Exception) {}
        try { bassBoost?.release() } catch (ignored: Exception) {}
        try { virtualizer?.release() } catch (ignored: Exception) {}
        try { presetReverb?.release() } catch (ignored: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
    }

    fun release() {
        releaseInternal()
        currentSessionId = 0
    }
}
