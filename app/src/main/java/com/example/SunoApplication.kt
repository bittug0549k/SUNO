package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.local.SunoDatabase
import com.example.data.repository.MusicRepository
import com.example.data.repository.PreferencesRepository

class SunoApplication : Application(), ImageLoaderFactory {

    lateinit var database: SunoDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = SunoDatabase.getDatabase(this)
        musicRepository = MusicRepository(this, database.musicDao())
        preferencesRepository = PreferencesRepository(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("album_art_cache"))
                    .maxSizeBytes(30L * 1024 * 1024) // 30 MB disk cache
                    .build()
            }
            .allowHardware(true)
            .respectCacheHeaders(false)
            .crossfade(250)
            .build()
    }

    companion object {
        lateinit var instance: SunoApplication
            private set
    }
}
