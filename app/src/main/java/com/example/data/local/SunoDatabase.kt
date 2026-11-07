package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entity.CustomFolderEntity
import com.example.data.local.entity.ExcludedFolderEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.SongPlayCountEntity
import com.example.data.local.entity.TagOverrideEntity

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        TagOverrideEntity::class,
        PlaybackHistoryEntity::class,
        SongPlayCountEntity::class,
        ExcludedFolderEntity::class,
        CustomFolderEntity::class,
        com.example.data.local.entity.ListeningSession::class,
        com.example.data.local.entity.EqualizerProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SunoDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: SunoDatabase? = null

        fun getDatabase(context: Context): SunoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SunoDatabase::class.java,
                    "suno_player_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
