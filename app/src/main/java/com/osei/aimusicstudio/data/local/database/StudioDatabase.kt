package com.osei.aimusicstudio.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.osei.aimusicstudio.data.local.dao.AudioProjectDao
import com.osei.aimusicstudio.data.local.dao.ProjectDao
import com.osei.aimusicstudio.data.local.dao.SongDao
import com.osei.aimusicstudio.data.local.dao.UserProfileDao
import com.osei.aimusicstudio.data.local.dao.VideoProjectDao
import com.osei.aimusicstudio.data.local.entity.LocalAudioProjectEntity
import com.osei.aimusicstudio.data.local.entity.LocalSongEntity
import com.osei.aimusicstudio.data.local.entity.LocalUserProfileEntity
import com.osei.aimusicstudio.data.local.entity.LocalVideoProjectEntity

@Database(
    entities = [
        LocalSongEntity::class,
        LocalUserProfileEntity::class,
        LocalVideoProjectEntity::class,
        LocalAudioProjectEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class StudioDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun videoProjectDao(): VideoProjectDao
    abstract fun audioProjectDao(): AudioProjectDao
    abstract fun projectDao(): ProjectDao

    companion object {
        private const val DATABASE_NAME = "osei_music_studio.db"

        @Volatile
        private var INSTANCE: StudioDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add tempo to local_songs
                db.execSQL("ALTER TABLE local_songs ADD COLUMN tempo TEXT NOT NULL DEFAULT 'Moderate (110 BPM)'")
                // 2. Create local_video_projects table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS local_video_projects (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        prompt TEXT NOT NULL,
                        visualStyle TEXT NOT NULL,
                        aspectRatio TEXT NOT NULL,
                        linkedSongId TEXT,
                        linkedSongTitle TEXT,
                        localAudioUri TEXT,
                        localVideoUri TEXT,
                        status TEXT NOT NULL,
                        durationSeconds INTEGER NOT NULL,
                        phaseVersion TEXT NOT NULL,
                        createdAtEpoch INTEGER NOT NULL,
                        updatedAtEpoch INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_video_projects_userId ON local_video_projects(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_video_projects_status ON local_video_projects(status)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create local_audio_projects table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS local_audio_projects (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        prompt TEXT NOT NULL DEFAULT '',
                        genre TEXT NOT NULL DEFAULT 'Ghanaian Highlife',
                        mood TEXT NOT NULL DEFAULT 'Uplifting',
                        tempo TEXT NOT NULL DEFAULT 'Moderate (110 BPM)',
                        vocalPreference TEXT NOT NULL DEFAULT 'Male',
                        language TEXT NOT NULL DEFAULT 'English',
                        lyrics TEXT NOT NULL DEFAULT '',
                        isInstrumental INTEGER NOT NULL DEFAULT 0,
                        localAudioUri TEXT,
                        audioUrl TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'draft',
                        durationSeconds INTEGER NOT NULL DEFAULT 0,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        phaseVersion TEXT NOT NULL DEFAULT 'phase_4_foundation',
                        createdAtEpoch INTEGER NOT NULL,
                        updatedAtEpoch INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'LOCAL_ONLY'
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_audio_projects_userId ON local_audio_projects(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_audio_projects_status ON local_audio_projects(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_audio_projects_syncStatus ON local_audio_projects(syncStatus)")
            }
        }

        fun getInstance(context: Context): StudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
